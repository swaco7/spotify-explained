package com.example.spotifyexplained.ui.topItems

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.spotifyexplained.R
import com.example.spotifyexplained.activity.MainActivity
import com.example.spotifyexplained.adapter.*
import com.example.spotifyexplained.databinding.FragmentSavedTracksBinding
import com.example.spotifyexplained.databinding.FragmentTopTracksBinding
import com.example.spotifyexplained.general.*
import com.example.spotifyexplained.model.*
import com.example.spotifyexplained.services.NetworkGraph
import com.example.spotifyexplained.ui.saved.ContextViewModelFactory
import com.faltenreich.skeletonlayout.Skeleton
import com.faltenreich.skeletonlayout.applySkeleton
import java.util.*

/**
This fragment is dedicated to user's saved tracks from Spotify
 */
@SuppressLint("SetJavaScriptEnabled")
class SavedSongsFragment : Fragment(), TrackDetailClickHandler, GraphClickHandler {
    private lateinit var viewModel: SavedSongsViewModel
    private lateinit var adapter: TracksAdapter
    private var _binding: FragmentSavedTracksBinding? = null
    private lateinit var webView: WebView
    private lateinit var genreColorList: RecyclerView
    private lateinit var bundleTrackList: RecyclerView
    private lateinit var featuresList: RecyclerView
    private lateinit var genresList: RecyclerView
    private lateinit var bundleLineInfoList: RecyclerView
    private val showDetailInfoFunc = { message: String -> this.showDetailInfo(message) }
    private val showBundleDetailInfoFunc = { tracks: String, message: String -> this.showBundleDetailInfo(tracks, message) }
    private val hideDetailInfoFunc = { this.hideDetailInfo() }
    private val finishLoadingFunc = { this.finishLoading() }
    private val showLineDetailInfoFunc = { message: String -> this.showLineDetailInfo(message) }
    private val binding get() = _binding!!
    private lateinit var skeleton: Skeleton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel = ViewModelProvider(
            this,
            ContextViewModelFactory(context as MainActivity)
        )[SavedSongsViewModel::class.java]
        _binding = FragmentSavedTracksBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        val tracksList: RecyclerView = binding.topTracksList
        webView = binding.webView
        webView.settings.javaScriptEnabled = true
        genreColorList = binding.root.findViewById(R.id.genreColorList)
        bundleTrackList = binding.root.findViewById(R.id.bundleTracksList)
        bundleTrackList.addItemDecoration(DividerItemDecoration(bundleTrackList.context, LinearLayoutManager.VERTICAL))
        featuresList = binding.root.findViewById(R.id.featuresLineRecycler)
        genresList = binding.root.findViewById(R.id.genresLineRecycler)
        bundleLineInfoList = binding.root.findViewById(R.id.bundleLineRecycler)


        //Main List
        adapter = TracksAdapter(viewModel.saved.value!!, this)
        tracksList.addItemDecoration(DividerItemDecoration(tracksList.context, LinearLayoutManager.VERTICAL))
        tracksList.adapter = adapter
        tracksList.itemAnimator = null
        viewModel.saved.observe(viewLifecycleOwner, adapter::updateData)
        skeleton = tracksList.applySkeleton(R.layout.skeleton_tracks_row, 20)
        skeleton.maskColor = Helper.getSkeletonColor(this.requireContext())
        skeleton.showSkeleton()

        //Click handlers
        binding.trackClickHandler = this
        binding.graphClickHandler = this

        //Observers
        viewModel.zoomType.observe(viewLifecycleOwner) { zoomType ->
            binding.zoomType = zoomType
            if (viewModel.nodes.value!!.isNotEmpty()) {
                drawD3Graph(
                    viewModel.nodes.value!!,
                    viewModel.links.value!!,
                    zoomType
                )
            }
        }
        viewModel.settings.observe(viewLifecycleOwner) {
            binding.settings = it
        }
        viewModel.links.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                drawD3Graph(
                    viewModel.nodes.value!!,
                    viewModel.links.value!!,
                    viewModel.zoomType.value!!
                )
            }
        }
        viewModel.loadingState.observe(viewLifecycleOwner) {
            if (it == LoadingState.SUCCESS){
                skeleton.showOriginal()
            } else if (it == LoadingState.LOADING){
                skeleton.showSkeleton()
            }
        }
        return binding.root
    }

    /**
     *  Loads graph html into the webView.
     *  @param [nodes] list of nodes in the graph.
     *  @param [links] list of links in the graph.
     *  @param [zoomType] type of the current selected zoom.
     */
    private fun drawD3Graph(
        nodes: ArrayList<D3Node>,
        links: ArrayList<D3Link>,
        zoomType: ZoomType
    ) {
        if (zoomType == ZoomType.RESPONSIVE) {
            viewModel.graphLoadingState.value = LoadingState.LOADING
        }
        val rawHtml = NetworkGraph.getHeader() +
                NetworkGraph.addData(links.toString(), nodes.toString()) +
                NetworkGraph.getMainSVG() +
                NetworkGraph.getBaseSimulation(Constants.topTracksManyBody, Constants.topTracksCollisions) +
                NetworkGraph.getBody() +
                if (zoomType == ZoomType.RESPONSIVE) {
                    NetworkGraph.getTickWithZoom() + NetworkGraph.getZoomFeatures()
                } else {
                    NetworkGraph.getTick() + NetworkGraph.getZoom()
                } +
                NetworkGraph.getBundleLines() +
                NetworkGraph.getHighlights() +
                NetworkGraph.getTextWrap() +
                NetworkGraph.getFooter()
        val encodedHtml = Base64.getEncoder().encodeToString(rawHtml.toByteArray())
        webView.loadData(encodedHtml, Constants.mimeType, Constants.encoding)
        webView.addJavascriptInterface(
            JsWebInterface(
                requireContext(),
                showDetailInfoFunc,
                hideDetailInfoFunc,
                showBundleDetailInfoFunc,
                finishLoadingFunc,
                showLineDetailInfoFunc
            ), Constants.jsAppName
        )
    }

    /**
     * Opens node detail info window on the graph.
     * @param [currentIndex] index of the selected node in the list of all nodes.
     */
    private fun showDetailInfo(currentIndex: String?) {
        (context as MainActivity).runOnUiThread {
            val selectedTrack = viewModel.allGraphNodes.value!![currentIndex!!.toInt()]
            val genresColors = viewModel.showDetailInfoTrack(selectedTrack.track)
            genreColorList.adapter = GenreColorAdapter(genresColors)
            viewModel.detailViewVisible.value = true
            viewModel.detailVisibleType.value = DetailVisibleType.SINGLE
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
            val adapter = BundleAdapter(bundleItems)
            bundleTrackList.adapter = adapter
        }
    }

    /**
     * Opens line detail info window on the graph.
     * @param [message] indices of both nodes connected by the selected line in the list of all nodes.
     */
    private fun showLineDetailInfo(message: String?) {
        (context as MainActivity).runOnUiThread {
            viewModel.showLineDetailInfo(message)
            when (message!!.split(",").last()) {
                "GENRE" -> genresList.adapter = GenreColorAdapter(Helper.getGenreColorList(viewModel.lineGenreInfo.value!!.genres, viewModel.genreColorMap))
                "FEATURE" -> featuresList.adapter = FeaturesLineInfoAdapter(viewModel.lineFeaturesInfo.value!!.features!!.map { Pair(it.first.name, it.second)})
                "BUNDLE" -> bundleLineInfoList.adapter = BundleLineAdapter(viewModel.lineBundleInfo.value!!.items)
            }
        }
    }

    private fun hideDetailInfo() {
        (context as MainActivity).runOnUiThread {
            viewModel.detailViewVisible.value = false
        }
    }

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

    /**
     * Navigates to track detail page
     * @param [track] selected track object
     */
    override fun onTrackClick(track: Track?) {
        val action =
            TopItemsFragmentDirections.actionNavigationTopSongsToFragmentRecommendTrackDetailPage(
                track!!.trackId
            )
        NavHostFragment.findNavController(this).navigate(action)
        hideDetailInfo()
    }

    override fun onTrackIconClick() {
        val currentSettings = viewModel.settings.value!!
        currentSettings.artistsSelected = !currentSettings.artistsSelected
        viewModel.settings.value = currentSettings
        viewModel.drawGraph()
    }

    override fun onGenreIconClick() {
        val currentSettings = viewModel.settings.value!!
        currentSettings.genresFlag = !currentSettings.genresFlag
        viewModel.settings.value = currentSettings
        viewModel.drawGraph()
    }

    override fun onFeatureIconClick() {
        val currentSettings = viewModel.settings.value!!
        currentSettings.featuresFlag = !currentSettings.featuresFlag
        viewModel.settings.value = currentSettings
        viewModel.drawGraph()
    }

    override fun onRelatedIconClick() {
        val currentSettings = viewModel.settings.value!!
        currentSettings.relatedFlag = !currentSettings.relatedFlag
        viewModel.settings.value = currentSettings
        viewModel.drawGraph()
    }

    override fun onInfoIconClick() {
        viewModel.detailViewVisible.value = true
        viewModel.detailVisibleType.value = DetailVisibleType.INFO
    }
}