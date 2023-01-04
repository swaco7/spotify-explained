package com.example.spotifyexplained.ui.recommend.custom.specific

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import android.webkit.WebView
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.core.view.GestureDetectorCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.spotifyexplained.R
import com.example.spotifyexplained.activity.MainActivity
import com.example.spotifyexplained.adapter.*
import com.example.spotifyexplained.database.entity.TrackSpecificEntity
import com.example.spotifyexplained.databinding.FragmentRecommendCustomSpecificBinding
import com.example.spotifyexplained.general.*
import com.example.spotifyexplained.model.*
import com.example.spotifyexplained.ui.recommend.custom.base.CustomRecommendBaseFragmentDirections
import com.example.spotifyexplained.general.TrackDatabaseViewModelFactory
import com.example.spotifyexplained.model.enums.BundleItemType
import com.example.spotifyexplained.model.enums.LoadingState
import com.example.spotifyexplained.model.enums.ZoomType
import com.example.spotifyexplained.html.GraphHtmlBuilder
import com.faltenreich.skeletonlayout.Skeleton
import com.faltenreich.skeletonlayout.applySkeleton
import java.util.*

@SuppressLint("SetJavaScriptEnabled")
class CustomRecommendSpecificFragment : Fragment(), TrackDetailClickHandler, GraphClickHandler {
    private lateinit var viewModel: CustomRecommendSpecificViewModel
    private lateinit var adapter: TracksRecommendedBaseDatabaseAdapter<TrackSpecificEntity>
    private var _binding: FragmentRecommendCustomSpecificBinding? = null
    private val binding get() = _binding!!
    private lateinit var trackFeaturesList: RecyclerView
    private lateinit var bundleTrackList: RecyclerView
    private val showDetailInfoFunc = { message: String -> this.showDetailInfo(message) }
    private val showBundleDetailInfoFunc = { tracks: String, message: String -> this.showBundleDetailInfo(tracks, message) }
    private val hideDetailInfoFunc = { this.hideDetailInfo() }
    private val finishLoadingFunc = { this.finishLoading() }
    private val showLineDetailInfoFunc = { _: String -> this.showLineDetailInfo() }
    private lateinit var skeleton: Skeleton

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(
            this,
            TrackDatabaseViewModelFactory(
                context as MainActivity,
                ((context as MainActivity).application as App).repository
            )
        )[CustomRecommendSpecificViewModel::class.java]
        _binding = FragmentRecommendCustomSpecificBinding.inflate(inflater, container, false)
        val recommendedList: RecyclerView = binding.recommendList
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        val webView = binding.webView
        webView.settings.javaScriptEnabled = true
        trackFeaturesList = binding.root.findViewById(R.id.genreColorList)
        bundleTrackList = binding.root.findViewById(R.id.bundleTracksList)
        bundleTrackList.addItemDecoration(
            DividerItemDecoration(
                bundleTrackList.context,
                LinearLayoutManager.VERTICAL
            )
        )

        //Main List
        adapter = TracksRecommendedBaseDatabaseAdapter(this)
        recommendedList.addItemDecoration(
            DividerItemDecoration(
                recommendedList.context,
                LinearLayoutManager.VERTICAL
            )
        )
        recommendedList.adapter = adapter
        recommendedList.itemAnimator = null
        skeleton = recommendedList.applySkeleton(R.layout.skeleton_tracks_simplified_row, 20)
        skeleton.maskColor = Helper.getSkeletonColor(this.requireContext())
        skeleton.showSkeleton()

        //Click handlers
        binding.trackClickHandler = this
        binding.graphClickHandler = this

        //Observers
        (context as MainActivity).viewModel.expanded.observe(viewLifecycleOwner) {
            viewModel.expanded.value = it
        }
        (context as MainActivity).viewModel.tabVisible.observe(viewLifecycleOwner) {
            viewModel.tabVisible.value = it
        }
        viewModel.tracksFromDatabaseLiveData.observe(viewLifecycleOwner) { tracks ->
            tracks.let { adapter.submitList(it) }
        }
        viewModel.zoomType.observe(viewLifecycleOwner) { zoomType ->
            binding.zoomType = zoomType
            if (viewModel.nodes.value!!.isNotEmpty()) {
                drawD3Graph(
                    viewModel.nodes.value!!,
                    viewModel.linksDistance.value!!,
                    viewModel.zoomType.value!!,
                    webView
                )
            }
        }
        viewModel.linksDistance.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                drawD3Graph(
                    viewModel.nodes.value!!,
                    viewModel.linksDistance.value!!,
                    viewModel.zoomType.value!!,
                    webView
                )
            }
        }
        binding.root.findViewById<ImageView>(R.id.reload_button).setOnClickListener {
            adapter.submitList(null)
            viewModel.clearData()
        }
        viewModel.loadingState.observe(viewLifecycleOwner) {
            if (it == LoadingState.SUCCESS){
                onDataLoaded()
            } else if (it == LoadingState.LOADING){
                skeleton.showSkeleton()
            }
        }
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun onDataLoaded(){
        skeleton.showOriginal()
    }

    /**
     * Navigates to track detail page
     * @param [track] selected track object
     */
    override fun onTrackClick(track: Track?) {
        val action =
            CustomRecommendBaseFragmentDirections.actionNavigationCustomRecommendToFragmentRecommendTrackDetailPage(
                track!!.trackId
            )
        NavHostFragment.findNavController(this).navigate(action)
        hideDetailInfo()
    }

    /**
     *  Loads graph html into the webView.
     *  @param [nodes] list of nodes in the graph.
     *  @param [links] list of links in the graph.
     *  @param [zoomType] type of the current selected zoom.
     */
    private fun drawD3Graph(
        nodes: ArrayList<D3ForceNode>,
        links: ArrayList<D3ForceLinkDistance>,
        zoomType: ZoomType,
        webView: WebView
    ) {
        if (zoomType == ZoomType.RESPONSIVE) {
            viewModel.graphLoadingState.value = LoadingState.LOADING
        }
        val encodedHtml = GraphHtmlBuilder.buildFeaturesGraph(links, nodes, zoomType)
        webView.loadData(encodedHtml, Config.mimeType, Config.encoding)
        webView.addJavascriptInterface(
            JsWebInterface(
                requireContext(),
                showDetailInfoFunc,
                hideDetailInfoFunc,
                showBundleDetailInfoFunc,
                finishLoadingFunc,
                showLineDetailInfoFunc
            ), Config.jsAppName
        )
    }

    /**
     * Opens node detail info window on the graph.
     * @param [currentIndex] index of the selected node in the list of all nodes.
     */
    private fun showDetailInfo(currentIndex: String?) {
        (context as MainActivity).runOnUiThread {
            val selectedTrack = viewModel.allGraphNodes.value!![currentIndex!!.toInt()]
            if (selectedTrack.bundleItemType == BundleItemType.TRACK) {
                viewModel.showDetailInfo(selectedTrack.track!!)
                trackFeaturesList.adapter =
                    FeaturesAdapter(selectedTrack.audioFeatures.sortedByDescending { it.second })
            }
        }
    }

    /**
     * Opens bundle detail info window on the graph.
     * @param [nodeIndices] indices of the nodes contained in the bundle; indices are separated by ','.
     * @param [currentIndex] index of the selected node in the list of all nodes.
     */
    private fun showBundleDetailInfo(nodeIndices: String, currentIndex: String) {
        (context as MainActivity).runOnUiThread {
            val bundleItems = viewModel.showBundleDetailInfo(nodeIndices, currentIndex)
            val adapter = TrackFeatureBundleAdapter(bundleItems)
            bundleTrackList.adapter = adapter
        }
    }

    private fun showLineDetailInfo() {
        (context as MainActivity).runOnUiThread {}
    }

    private fun hideDetailInfo() {
        (context as MainActivity).runOnUiThread {
            viewModel.detailViewVisible.value = false
        }
    }

    /**
     * Hides loading bar when graph is ready.
     */
    private fun finishLoading() {
        (context as MainActivity).runOnUiThread {
            viewModel.graphLoadingState.value = LoadingState.GRAPH_LOADED
        }
    }

    override fun onBasicZoomClick() {
        viewModel.zoomType.value = ZoomType.BASIC
    }

    override fun onResponsiveZoomClick() {
        viewModel.zoomType.value = ZoomType.RESPONSIVE
    }

    override fun onCloseBundleClick() {
        hideDetailInfo()
    }


}