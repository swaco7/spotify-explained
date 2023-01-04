package com.example.spotifyexplained.ui.topItems

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.spotifyexplained.R
import com.example.spotifyexplained.activity.MainActivity
import com.example.spotifyexplained.adapter.*
import com.example.spotifyexplained.databinding.FragmentTopArtistsBinding
import com.example.spotifyexplained.general.*
import com.example.spotifyexplained.model.*
import com.example.spotifyexplained.model.enums.DetailVisibleType
import com.example.spotifyexplained.model.enums.LoadingState
import com.example.spotifyexplained.model.enums.ZoomType
import com.example.spotifyexplained.html.GraphHtmlBuilder
import com.faltenreich.skeletonlayout.Skeleton
import com.faltenreich.skeletonlayout.applySkeleton
import java.util.*

/**
 * This fragment is dedicated to user's top tracks by Spotify
 */
@SuppressLint("SetJavaScriptEnabled")
class TopArtistsFragment : Fragment(), GraphClickHandler {
    private lateinit var viewModel: TopArtistsViewModel
    private lateinit var adapter: ArtistsAdapter
    private var _binding: FragmentTopArtistsBinding? = null
    private lateinit var webView: WebView
    private lateinit var genreColorList: RecyclerView
    private lateinit var bundleTrackList: RecyclerView
    private lateinit var genresList: RecyclerView
    private lateinit var bundleLineInfoList: RecyclerView
    private lateinit var colorInfoList : RecyclerView

    private val showDetailInfoFunc = { message: String -> this.showDetailInfo(message) }
    private val showBundleDetailInfoFunc = { tracks: String, message: String -> this.showBundleDetailInfo(tracks, message) }
    private val hideDetailInfoFunc = { this.hideDetailInfo() }
    private val finishLoadingFunc = { this.finishLoading() }
    private val showLineDetailInfoFunc = { message: String -> this.showLineDetailInfo(message) }
    private lateinit var skeleton: Skeleton

    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel = ViewModelProvider(
            this,
            ContextViewModelFactory(context as MainActivity)
        )[TopArtistsViewModel::class.java]
        _binding = FragmentTopArtistsBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        val tracksList: RecyclerView = binding.topTracksList
        webView = binding.webView
        webView.settings.javaScriptEnabled = true
        genreColorList = binding.root.findViewById(R.id.genreColorList)
        bundleTrackList = binding.root.findViewById(R.id.bundleTracksList)
        bundleTrackList.addItemDecoration(DividerItemDecoration(bundleTrackList.context, LinearLayoutManager.VERTICAL))
        genresList = binding.root.findViewById(R.id.genresLineRecycler)
        bundleLineInfoList = binding.root.findViewById(R.id.bundleLineRecycler)
        colorInfoList = binding.root.findViewById(R.id.colorInfoRecycler)

        //Main List
        adapter = ArtistsAdapter(viewModel.artists.value!!)
        tracksList.addItemDecoration(DividerItemDecoration(tracksList.context, LinearLayoutManager.VERTICAL))
        tracksList.adapter = adapter
        tracksList.itemAnimator = null
        viewModel.artists.observe(viewLifecycleOwner, adapter::updateData)
        skeleton = tracksList.applySkeleton(R.layout.skeleton_artist_row, 20)
        skeleton.maskColor = Helper.getSkeletonColor(this.requireContext())
        skeleton.showSkeleton()

        //Click handlers
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
            if (it.isNotEmpty() || !viewModel.nodes.value.isNullOrEmpty()) {
                drawD3Graph(
                    viewModel.nodes.value!!,
                    viewModel.links.value!!,
                    viewModel.zoomType.value!!
                )
            }
        }
        viewModel.loadingState.observe(viewLifecycleOwner) {
            if (it == LoadingState.SUCCESS){
                if (viewModel.links.value.isNullOrEmpty() && viewModel.nodes.value.isNullOrEmpty()){
                    viewModel.graphLoadingState.value = LoadingState.FAILURE
                }
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
    private fun drawD3Graph(nodes: ArrayList<D3ForceNode>, links: ArrayList<D3ForceLink>, zoomType: ZoomType) {
        if (zoomType == ZoomType.RESPONSIVE) {
            viewModel.graphLoadingState.value = LoadingState.LOADING
        }
        val encodedHtml = GraphHtmlBuilder.buildBaseGraph(
            links,
            nodes,
            zoomType,
            Config.topTracksManyBody,
            Config.topTracksCollisions
        )
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
            val selectedArtist = viewModel.allGraphNodes.value!![currentIndex!!.toInt()]
            val genresColors = viewModel.showDetailInfo(selectedArtist)
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

    override fun onBasicZoomClick() { viewModel.zoomType.value = ZoomType.BASIC }

    override fun onResponsiveZoomClick() { viewModel.zoomType.value = ZoomType.RESPONSIVE }

    override fun onCloseBundleClick() { hideDetailInfo() }

    override fun onGenreIconClick() {
        val currentSettings = viewModel.settings.value!!
        currentSettings.genresFlag = !currentSettings.genresFlag
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

    override fun onColorInfoClick() {
        val result = ColorHelper.sortColorsByHue(viewModel.genreColorMap)
        colorInfoList.adapter = GenreColorAdapter(result?.map { GenreColor(it.key, Helper.getColorFromMap(it.key, result)) })
        viewModel.detailViewVisible.value = true
        viewModel.detailVisibleType.value = DetailVisibleType.COLORINFO
    }
}