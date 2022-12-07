package com.example.spotifyexplained.ui.recommend.spotify.combined

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cruxlab.sectionedrecyclerview.lib.SectionDataManager
import com.cruxlab.sectionedrecyclerview.lib.SectionHeaderLayout
import com.example.spotifyexplained.R
import com.example.spotifyexplained.activity.MainActivity
import com.example.spotifyexplained.adapter.*
import com.example.spotifyexplained.database.CombinedRecommendEntity
import com.example.spotifyexplained.databinding.FragmentRecommendCombinedBinding
import com.example.spotifyexplained.general.*
import com.example.spotifyexplained.model.*
import com.example.spotifyexplained.ui.recommend.spotify.RecommendFragmentDirections
import com.example.spotifyexplained.general.TrackDatabaseViewModelFactory
import com.example.spotifyexplained.model.enums.*
import com.example.spotifyexplained.services.GraphHtmlBuilder
import com.faltenreich.skeletonlayout.applySkeleton
import java.util.*

/**
This fragment is for recommendations from Spotify based on custom combined seeds
 */
@SuppressLint("SetJavaScriptEnabled")
class CombinedRecommendFragment : Fragment(), TrackDetailClickHandler, GraphClickHandler {
    private lateinit var viewModel: CombinedRecommendViewModel
    private var _binding: FragmentRecommendCombinedBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: TracksRecommendedBaseDatabaseAdapter<CombinedRecommendEntity>
    private lateinit var webView: WebView
    private lateinit var genreColorList: RecyclerView
    private lateinit var bundleTrackList: RecyclerView
    private lateinit var featuresList: RecyclerView
    private lateinit var genresList: RecyclerView
    private lateinit var sectionDataManager : SectionDataManager
    private lateinit var bundleLineInfoList: RecyclerView
    private val showDetailInfoFunc = { message: String -> this.showDetailInfo(message) }
    private val showBundleDetailInfoFunc = { tracks: String, message: String -> this.showBundleDetailInfo(tracks, message) }
    private val hideDetailInfoFunc = { this.hideDetailInfo() }
    private val finishLoadingFunc = { this.finishLoading() }
    private val showLineDetailInfoFunc = { message: String -> this.showLineDetailInfo(message) }
    private val showMetricsInfoFunc = {message: String -> this.showMetrics(message)}

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewModel = ViewModelProvider(
            this,
            TrackDatabaseViewModelFactory(
                context as MainActivity,
                ((context as MainActivity).application as App).repository
            )
        )[CombinedRecommendViewModel::class.java]
        _binding = FragmentRecommendCombinedBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        val recommendedList: RecyclerView = binding.recommendList
        webView = binding.webView
        webView.settings.javaScriptEnabled = true
        genreColorList = binding.root.findViewById(
            R.id.genreColorList)
        bundleTrackList = binding.root.findViewById(R.id.bundleTracksList)
        bundleTrackList.addItemDecoration(
            DividerItemDecoration(
                bundleTrackList.context,
                LinearLayoutManager.VERTICAL
            )
        )
        featuresList = binding.root.findViewById(R.id.featuresLineRecycler)
        genresList = binding.root.findViewById(R.id.genresLineRecycler)
        bundleLineInfoList = binding.root.findViewById(R.id.bundleLineRecycler)

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
        val skeleton = recommendedList.applySkeleton(R.layout.skeleton_tracks_simplified_row, 20)
        skeleton.maskColor = Helper.getSkeletonColor(this.requireContext())
        skeleton.showSkeleton()

        //Click handlers
        binding.trackClickHandler = this
        binding.graphClickHandler = this

        val sectionHeaderLayout: SectionHeaderLayout = binding.sectionHeaderLayout
        val settingsRecycler: RecyclerView = binding.settingsList

        //Observers
        viewModel.tracksFromDatabaseLiveData.observe(viewLifecycleOwner) { tracks ->
            tracks.let { adapter.submitList(it) }
        }
        viewModel.settings.observe(viewLifecycleOwner) {
            binding.settings = it
        }
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
        viewModel.links.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                drawD3Graph(
                    viewModel.nodes.value!!,
                    viewModel.links.value!!,
                    viewModel.zoomType.value!!
                )
            }
        }
        binding.reloadLayout.setOnClickListener {
            adapter.submitList(null)
            viewModel.clearData()
        }
        val searchView = binding.searchView
        val searchButton = binding.searchButton
        searchButton.setOnClickListener {
            searchView.visibility = View.VISIBLE
            searchView.isIconified = false
        }
        searchView.setOnCloseListener {
            searchView.visibility = View.GONE
            false
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                filter(newText)
                return false
            }
        })
        viewModel.selectedIds.observe(viewLifecycleOwner) {
            binding.itemSelected = it.isNotEmpty()
        }

        settingsRecycler.setHasFixedSize(false)
        sectionDataManager = SectionDataManager()
        settingsRecycler.adapter = sectionDataManager.adapter
        sectionHeaderLayout.attachTo(settingsRecycler, sectionDataManager)

        viewModel.loadingState.observe(viewLifecycleOwner) { state ->
            if (state == LoadingState.SETTINGS_LOADED) {
                val tracksSectionAdapter =
                    SimpleSectionAdapter(isHeaderVisible = true, isHeaderPinned = true,
                        viewModel.topTracks.value!!.map { it.track }.sortedBy { it.trackName }
                            .map {
                                Pair(
                                    "${it.trackName} - ${it.artists[0].artistName}",
                                    it.trackId
                                )
                            },
                        getString(R.string.tracks), viewModel, recommendSeedType = RecommendSeedType.TRACK
                    )
                val artistSectionAdapter =
                    SimpleSectionAdapter(isHeaderVisible = true, isHeaderPinned = true,
                        viewModel.topArtists.value!!.sortedBy { it.artistName }
                            .map { Pair(it.artistName, it.artistId) },
                        getString(R.string.artists), viewModel, recommendSeedType = RecommendSeedType.ARTIST
                    )
                val genresSectionAdapter =
                    SimpleSectionAdapter(isHeaderVisible = true, isHeaderPinned = true,
                        viewModel.availableGenres.value!!.sortedBy { it }.map { Pair(it, it) },
                        getString(R.string.genres), viewModel, recommendSeedType = RecommendSeedType.GENRE
                    )
                sectionDataManager.addSection(tracksSectionAdapter, 2)
                sectionDataManager.addSection(artistSectionAdapter, 2)
                sectionDataManager.addSection(genresSectionAdapter, 2)
            }
        }
        viewModel.loadingState.observe(viewLifecycleOwner) {
            if (it == LoadingState.SUCCESS){
                skeleton.showOriginal()
            } else if (it == LoadingState.LOADING){
                skeleton.showSkeleton()
            }
        }

        viewModel.metricLinks.observe(viewLifecycleOwner){
            if (it.isNotEmpty()){
                calcMetrics(0)
            }
        }
        binding.metricsButton.setOnClickListener {
            viewModel.prepareNodesAndEdgesForMetrics()
        }
        viewModel.metricIndex.observe(viewLifecycleOwner){
            if (it > 0){
                calcMetrics(it)
            }
        }
        return binding.root
    }

    /**
     * Filter items in the list based on the search query
     * @param query search query
     */
    private fun filter(query : String) {
        val tracksSectionAdapter = SimpleSectionAdapter(isHeaderVisible = true, isHeaderPinned = true,
            viewModel.topTracks.value!!.map { it.track }.filter { it.trackName.lowercase(Locale.getDefault()).contains(query) ||
                    it.artists[0].artistName.lowercase(Locale.getDefault()).contains(query)}.sortedBy { it.trackName }
                .map { Pair("${it.trackName} - ${it.artists[0].artistName}", it.trackId) },
            getString(R.string.tracks), viewModel, recommendSeedType = RecommendSeedType.TRACK
        )
        val artistSectionAdapter = SimpleSectionAdapter(isHeaderVisible = true, isHeaderPinned = true,
            viewModel.topArtists.value!!.filter { it.artistName.lowercase(Locale.getDefault()).contains(query) }.sortedBy { it.artistName }
                .map { Pair(it.artistName, it.artistId) },
            getString(R.string.artists), viewModel, recommendSeedType = RecommendSeedType.ARTIST
        )
        val genresSectionAdapter = SimpleSectionAdapter(isHeaderVisible = true, isHeaderPinned = true,
            viewModel.availableGenres.value!!.filter { it.lowercase(Locale.getDefault()).contains(query) }.sortedBy { it }.map { Pair(it, it) },
            getString(R.string.genres), viewModel, recommendSeedType = RecommendSeedType.GENRE
        )
        if (sectionDataManager.sectionCount == 3) {
            sectionDataManager.replaceSection(0, tracksSectionAdapter, 2)
            sectionDataManager.replaceSection(1, artistSectionAdapter, 2)
            sectionDataManager.replaceSection(2, genresSectionAdapter, 2)
        }
    }
    /**
     * Navigates to track detail page
     * @param [track] selected track object
     */
    override fun onTrackClick(track: Track?) {
        val action = RecommendFragmentDirections.actionNavigationRecommendedSongsToFragmentRecommendTrackDetailPage(track!!.trackId)
        requireView().findNavController().navigate(action)
        hideDetailInfo()
    }

    /**
     *  Gathers and loads graph html into the webView.
     *  @param [nodes] list of nodes in the graph.
     *  @param [links] list of links in the graph.
     *  @param [zoomType] type of the current selected zoom.
     */
    private fun drawD3Graph(
        nodes: ArrayList<D3ForceNode>,
        links: ArrayList<D3ForceLink>,
        zoomType: ZoomType
    ) {
        if (zoomType == ZoomType.RESPONSIVE) {
            viewModel.graphLoadingState.value = LoadingState.LOADING
        }
        val encodedHtml = GraphHtmlBuilder.buildBaseGraph(
            links,
            nodes,
            zoomType,
            Config.combinedRecommendManyBody,
            Config.combinedRecommendCollisions
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
            if (viewModel.allGraphArtistNodes.value!!.isNotEmpty()) {
                val selectedArtist = viewModel.allGraphArtistNodes.value!![currentIndex!!.toInt()]
                val genresColors = viewModel.showDetailInfo(selectedArtist)
                genreColorList.adapter = GenreColorAdapter(genresColors)
            } else {
                val selectedTrack = viewModel.allGraphTrackNodes.value!![currentIndex!!.toInt()]
                val genresColors = viewModel.showDetailInfoTrack(selectedTrack.track)
                genreColorList.adapter = GenreColorAdapter(genresColors)
            }
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
            bundleTrackList.adapter = BundleAdapter(bundleItems)
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
                "BUNDLE" -> bundleLineInfoList.adapter = BundleLineAdapter(viewModel.lineBundleInfo.value!!.items, this)
            }
        }
    }

    /**
     * Hides detail info window.
     */
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

    /**
     * Sets zoom type to basic.
     */
    override fun onBasicZoomClick() {
        viewModel.zoomType.value = ZoomType.BASIC
    }

    /**
     * Sets zoom type to responsive.
     */
    override fun onResponsiveZoomClick() {
        viewModel.zoomType.value = ZoomType.RESPONSIVE
    }

    /**
     * Hides detail info window.
     */
    override fun onCloseBundleClick() {
        hideDetailInfo()
    }

    override fun onTrackIconClick() {
        viewModel.settingsChanged(SettingsItemType.TRACK)
    }

    override fun onGenreIconClick() {
        viewModel.settingsChanged(SettingsItemType.GENRE)
    }

    override fun onFeatureIconClick() {
        viewModel.settingsChanged(SettingsItemType.FEATURE)
    }

    override fun onRelatedIconClick() {
        viewModel.settingsChanged(SettingsItemType.RELATED)
    }

    override fun onInfoIconClick() {
        viewModel.detailViewVisible.value = true
        viewModel.detailVisibleType.value = DetailVisibleType.INFO
    }


    //-------- Metrics code ----------------//
    private fun showMetrics(message: String?) {
        (context as MainActivity).runOnUiThread {
            val latexOutput = message!!.replace(",", " & ")
            Log.e("metrics", latexOutput)
            if (viewModel.metricIndex.value!! < 15) {
                viewModel.metricIndex.value = viewModel.metricIndex.value!! + 1
            }
        }
    }

    private fun calcMetrics(index : Int){
        drawMetrics(viewModel.metricNodes.value!!, viewModel.metricLinks.value!!, Config.manyBody[index % 4], Config.collisions[(index / 4)])
    }

    private fun drawMetrics(
        nodes: ArrayList<D3ForceNode>,
        links: ArrayList<D3ForceLink>,
        manyBody : Int,
        colls: Float
    ) {
        val encodedHtml = GraphHtmlBuilder.buildMetricsGraph(
            links,
            nodes,
            manyBody,
            colls
        )
        webView.loadData(encodedHtml, Config.mimeType, Config.encoding)
        webView.addJavascriptInterface(
            JsWebInterface(
                requireContext(),
                showDetailInfoFunc,
                hideDetailInfoFunc,
                showBundleDetailInfoFunc,
                finishLoadingFunc,
                showLineDetailInfoFunc,
                showMetricsInfoFunc
            ), Config.jsAppName
        )
    }
}