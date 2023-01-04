package com.example.spotifyexplained.ui.recommend.spotify.combined

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import com.example.spotifyexplained.activity.MainActivity
import com.example.spotifyexplained.database.entity.CombinedRecommendEntity
import com.example.spotifyexplained.general.Config
import com.example.spotifyexplained.general.GraphInfoHelper
import com.example.spotifyexplained.general.Helper
import com.example.spotifyexplained.general.VisualTabClickHandler
import com.example.spotifyexplained.model.*
import com.example.spotifyexplained.model.enums.*
import com.example.spotifyexplained.services.ApiRepository
import com.example.spotifyexplained.repository.TrackRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * ViewModel for recommendations from Spotify based on track seed
 * @param [activity] reference to MainActivity
 * @property [repository] repository for database
 */
class CombinedRecommendViewModel(activity: Activity, private val repository: TrackRepository) : ViewModel(), VisualTabClickHandler {
    val visualState = MutableLiveData<VisualState>().apply { value = VisualState.SETTINGS }
    val loadingState = MutableLiveData<LoadingState>().apply { value = LoadingState.LOADING }
    val graphLoadingState = MutableLiveData<LoadingState>().apply { value = LoadingState.LOADING }
    val detailViewVisible = MutableLiveData<Boolean>().apply { value = false }
    val detailVisibleType = MutableLiveData<DetailVisibleType>()
    val lineInfo = MutableLiveData<LineDetailInfo>()
    val lineGenreInfo = MutableLiveData<LineDetailGenreInfo>()
    val lineFeaturesInfo = MutableLiveData<LineDetailFeatureInfo>()
    val lineBundleInfo = MutableLiveData<LineDetailBundleInfo>()
    val zoomType = MutableLiveData<ZoomType>().apply { value = ZoomType.RESPONSIVE }
    val isTrack = MutableLiveData<Boolean>().apply { value = false }
    val selectedTrack = MutableLiveData<Track>()
    val artistName = MutableLiveData<String>().apply { value = "" }
    val trackName = MutableLiveData<String>().apply { value = "" }
    val imageUrl = MutableLiveData<String>().apply { value = "" }
    val settings = MutableLiveData<GraphSettings>().apply { value = Config.graphSettings }

    val nodes: MutableLiveData<ArrayList<D3ForceNode>> by lazy {
        MutableLiveData<ArrayList<D3ForceNode>>(arrayListOf())
    }
    val links: MutableLiveData<ArrayList<D3ForceLink>> by lazy {
        MutableLiveData<ArrayList<D3ForceLink>>(arrayListOf())
    }
    val availableGenres: MutableLiveData<List<String>> by lazy {
        MutableLiveData<List<String>>(listOf())
    }
    var genreColorMap: HashMap<String, MixableColor> = HashMap()
    val metricNodes: MutableLiveData<ArrayList<D3ForceNode>> by lazy {
        MutableLiveData<ArrayList<D3ForceNode>>(arrayListOf())
    }
    val metricLinks: MutableLiveData<ArrayList<D3ForceLink>> by lazy {
        MutableLiveData<ArrayList<D3ForceLink>>(arrayListOf())
    }
    val metricIndex = MutableLiveData<Int>().apply { value = 0 }

    val allGraphArtistNodes = MutableLiveData<MutableList<Artist>>().apply{ mutableListOf<Artist>() }
    val allGraphTrackNodes = MutableLiveData<MutableList<TrackAudioFeatures>>().apply{ mutableListOf<TrackAudioFeatures>() }
    val topTracks: MutableLiveData<List<TrackAudioFeatures>> by lazy { MutableLiveData<List<TrackAudioFeatures>>(mutableListOf()) }
    val topArtists: MutableLiveData<List<Artist>> by lazy { MutableLiveData<List<Artist>>(mutableListOf()) }
    val selectedIds: MutableLiveData<MutableList<Pair<RecommendSeedType, String>>> by lazy {
        MutableLiveData<MutableList<Pair<RecommendSeedType, String>>>(
            mutableListOf()
        )
    }
    val tracksFromDatabaseLiveData: LiveData<MutableList<CombinedRecommendEntity>> = repository.allCombinedTracks.asLiveData()

    private lateinit var recommendedTracks: List<Track>
    private lateinit var recommendedTracksFeatures: List<TrackAudioFeatures>
    private val tracksFromDatabaseFlow: Flow<MutableList<CombinedRecommendEntity>> = repository.allCombinedTracks

    private val context: Context? by lazy {
        activity
    }

    private fun insertTracksBasedOnCombined(tracks: List<Track>) = viewModelScope.launch {
        repository.insertCombined(tracks.map { CombinedRecommendEntity(it.trackId, it) })
    }

    init {
        viewModelScope.launch {
            topArtists.value = (context as MainActivity).viewModel.topArtists.value
            topTracks.value = (context as MainActivity).viewModel.topTracks.value
            getAvailableGenreSeeds()
            loadingState.value = LoadingState.SETTINGS_LOADED
            tracksFromDatabaseFlow.collect { trackEntities ->
                if (trackEntities.isNotEmpty()) {
                    prepareGraph(trackEntities.map { it.track }.toMutableList())
                } else {
                    loadingState.value = LoadingState.FAILURE
                }
            }
        }
    }
    /**
     * Prepares full recommended tracks data
     */
    fun reload() {
        viewModelScope.launch {
            loadingState.value = LoadingState.LOADING
            topArtists.value = (context as MainActivity).viewModel.topArtists.value
            topTracks.value = (context as MainActivity).viewModel.topTracks.value
            Log.e("prepare", "--- Reload ---")
            recommendBasedOnTracks()
            recommendTracksGetFeatures()
            getAvailableGenreSeeds()
            getRecommendedTracksArtistGenres()
            getRecommendedTracksRelatedArtists()
            saveRecommendedTracksToDatabase()
            loadingState.value = LoadingState.RELOADED
        }
    }

    fun clearData() {
        viewModelScope.launch {
            repository.deleteCombined()
            reload()
        }
    }

    /**
     * Starts graph reloading after settings changed
     */
    fun drawGraph(){
        viewModelScope.launch {
            loadingState.value = LoadingState.LOADING
            prepareNodesAndEdges()
            loadingState.value = LoadingState.SUCCESS
        }
    }

    /**
     * Prepares graph data from received tracks
     * @param [tracks] tracks for graph generation
     */
    private fun prepareGraph(tracks: List<Track>) {
        viewModelScope.launch {
            loadingState.value = LoadingState.LOADING
            recommendedTracks = tracks
            recommendTracksGetFeatures()
            topArtists.value = (context as MainActivity).viewModel.topArtists.value
            Log.e("prepare", "--- Prepare Nodes and Edges ---")
            prepareNodesAndEdges()
            loadingState.value = LoadingState.SUCCESS
        }
    }

    /**
     *  Gets recommended tracks
     */
    private suspend fun recommendTracksGetFeatures() {
        recommendedTracksFeatures = ApiRepository.getTracksAudioFeatures(recommendedTracks, context!!) ?: listOf()
    }

    /**
     * Assigns artist data to each recommended track
     */
    private suspend fun getRecommendedTracksArtistGenres() {
        val chunkedTracks = recommendedTracks.chunked(50)
        var offset = 0
        for (chunk in chunkedTracks) {
            val response = ApiRepository.getArtistWithGenres(chunk.toMutableList(), context!!) ?: return
            for (i in response.artists.indices) {
                val index = i + offset
                recommendedTracks[index].trackGenres = response.artists[i].genres
                recommendedTracks[index].artists[0].genres = response.artists[i].genres
                recommendedTracks[index].artists[0].artistPopularity = response.artists[i].artistPopularity
                recommendedTracks[index].artists[0].images = response.artists[i].images
            }
            offset += response.artists.size
        }
    }

    /**
     * Gathers all graph elements
     */
    private suspend fun prepareNodesAndEdges(){
        val forceGraph : ForceGraph = if (settings.value!!.artistsSelected){
            Helper.prepareGraphArtists(settings.value!!, recommendedTracks, topArtists.value!!, context!!)
        } else {
            Helper.prepareGraphTracks(settings.value!!, recommendedTracksFeatures, topTracks.value!!.toMutableList(), context!!)
        }
        nodes.value = forceGraph.nodes
        genreColorMap = forceGraph.genreColorMap
        allGraphArtistNodes.value = forceGraph.nodeArtists.toMutableList()
        if (!settings.value!!.artistsSelected) {
            allGraphTrackNodes.value =
                ApiRepository.getTracksAudioFeatures(forceGraph.nodeTracks, context!!)?.toMutableList() ?: return
        }
        links.value = forceGraph.links
    }

    /**
     * Assigns related artist to each recommended track's artist
     */
    private suspend fun getRecommendedTracksRelatedArtists() {
        for (track in recommendedTracks) {
            val trackArtist = (context as MainActivity).viewModel.topArtists.value!!.firstOrNull { it.artistId == track.artists[0].artistId }
            track.artists[0].related_artists = trackArtist?.related_artists
                ?: (ApiRepository.getRelatedArtists(track.artists[0].artistId, context!!)?.toList()
                    ?: mutableListOf())
        }
    }

    private suspend fun getAvailableGenreSeeds() {
        val availableGenreSeeds = ApiRepository.getAvailableGenreSeeds(context!!) ?: GenreSeeds(arrayOf())
        availableGenres.value = availableGenreSeeds.seeds.toList()
    }
    /**
     * Calls repository to save tracks to database
     */
    private fun saveRecommendedTracksToDatabase() {
        insertTracksBasedOnCombined(recommendedTracks)
    }
    /**
     *  Gets recommended tracks
     */
    private suspend fun recommendBasedOnTracks() {
        val seedsChunked = selectedIds.value!!.chunked(5).toMutableList()
        val allRecommendedTracks = mutableListOf<Track>()
        for (chunk in seedsChunked) {
            val response = ApiRepository.getRecommendsCombined(chunk.toMutableList(), context!!) ?: return
            allRecommendedTracks.addAll(response.tracks)
        }
        recommendedTracks = allRecommendedTracks.distinct()
    }

    /**
     * Prepares data for graph detail window
     * @param [selectedArtist] selected node
     * @return list of genres with colors
     */
    fun showDetailInfo(selectedArtist: Artist): List<GenreColor>? {
        selectedTrack.value = tracksFromDatabaseLiveData.value!!.firstOrNull { it.track.artists[0].artistId == selectedArtist.artistId }?.track
        artistName.value = selectedArtist.artistName
        imageUrl.value = if (selectedArtist.images!!.isNotEmpty()) selectedArtist.images!![0].url else ""
        isTrack.value = selectedTrack.value != null
        return Helper.getGenreColorList(selectedArtist, genreColorMap)
    }

    /**
     * Prepares data for graph detail window
     * @param [selectedTrack] selected node
     * @return list of genres with colors
     */
    fun showDetailInfoTrack(selectedTrack: Track): List<GenreColor>? {
        this.selectedTrack.value = selectedTrack
        artistName.value = selectedTrack.artists[0].artistName
        imageUrl.value = if (selectedTrack.album.albumImages.isNotEmpty()) selectedTrack.album.albumImages[0].url else ""
        isTrack.value = true
        return Helper.getGenreColorList(selectedTrack.artists[0], genreColorMap)
    }

    /**
     * Prepares data for graph bundle detail window
     * @param [nodeIndices] indices of the nodes contained in the bundle; indices are separated by ','.
     * @param [currentIndex] index of the selected node in the list of all nodes.
     * @return items to display in the bundle list
     */
    fun showBundleDetailInfo(nodeIndices: String, currentIndex: String): MutableList<BundleGraphItem> {
        detailViewVisible.value = true
        detailVisibleType.value = DetailVisibleType.BUNDLE
        return GraphInfoHelper.showBundleDetailInfo(nodeIndices, currentIndex, allGraphArtistNodes.value!!, allGraphTrackNodes.value?.map { it.track } ?: listOf(), recommendedTracks, genreColorMap)
    }
    /**
     * Prepares data for line detail window
     * @param [nodeIndices] indices of the nodes contained in the bundle; indices are separated by ','.
     */
    fun showLineDetailInfo(nodeIndices: String?) {
        detailViewVisible.value = true
        when (nodeIndices!!.split(",").last()) {
            "RELATED" -> {
                lineInfo.value = GraphInfoHelper.showLineDetailInfo(nodeIndices, allGraphArtistNodes.value!!, allGraphTrackNodes.value ?: listOf())
                detailVisibleType.value = DetailVisibleType.LINE
            }
            "GENRE" -> {
                lineGenreInfo.value = GraphInfoHelper.showLineDetailGenreInfo(nodeIndices, allGraphArtistNodes.value!!, allGraphTrackNodes.value ?: listOf())
                detailVisibleType.value = DetailVisibleType.LINEGENRE
            }
            "FEATURE" -> {
                lineFeaturesInfo.value = GraphInfoHelper.showLineDetailFeatureInfo(nodeIndices, allGraphTrackNodes.value!!)
                detailVisibleType.value = DetailVisibleType.LINEFEATURE
            }
            "BUNDLE" -> {
                lineBundleInfo.value = if (settings.value!!.artistsSelected) {
                    GraphInfoHelper.showBundleLineInfoArtists(nodeIndices, allGraphArtistNodes.value!!, genreColorMap)
                } else {
                    GraphInfoHelper.showBundleLineInfoTracks(nodeIndices, allGraphTrackNodes.value!!, genreColorMap)
                }
                detailVisibleType.value = DetailVisibleType.LINEBUNDLE
            }
        }
    }
    /**
     * Change displayed content to list
     */
    override fun onListClick() {
        visualState.value = VisualState.TABLE
    }
    /**
     * Change displayed content to graph
     */
    override fun onGraphClick() {
        visualState.value = VisualState.GRAPH
    }
    /**
     * Change displayed content to settings page
     */
    override fun onSettingsClick() {
        visualState.value = VisualState.SETTINGS
    }

    fun settingsChanged(type : SettingsItemType){
        val currentSettings = settings.value!!
        when (type) {
            SettingsItemType.TRACK -> currentSettings.artistsSelected = !currentSettings.artistsSelected
            SettingsItemType.RELATED -> currentSettings.relatedFlag = !currentSettings.relatedFlag
            SettingsItemType.GENRE -> currentSettings.genresFlag = !currentSettings.genresFlag
            SettingsItemType.FEATURE -> currentSettings.featuresFlag = !currentSettings.featuresFlag
        }
        settings.value = currentSettings
        drawGraph()
    }

    /**
     *  Unused functions for graph generation for experiment
     * **/
    private fun trimNodes(
        nodeArtists: List<D3ForceNode>,
        edgesList: List<D3ForceLink>
    ): List<D3ForceNode> {
//        val nodesWithDegrees = mutableMapOf<Artist, Int>()
//        for (artist in nodeArtists) {
//            nodesWithDegrees[artist] =
//                edgesList.filter { it.first == artist || it.second == artist }.count()
//        }
//        val sorted = nodesWithDegrees.toList().sortedByDescending { (_, value) -> value }.map { it.first }
//            .dropLast(20)
        return nodeArtists.asSequence().shuffled().take(70).toList()
    }

    private fun trimEdges(
        edgesList: List<D3ForceLink>,
        nodeArtists: List<D3ForceNode>
    ): List<D3ForceLink> {
        val result =
            edgesList.filter { link -> nodeArtists.map { it.id }.contains(link.source) && nodeArtists.map { it.id }.contains(link.target) }
        return result.toMutableList()
    }

    fun prepareNodesAndEdgesForMetrics(){
        viewModelScope.launch {
            val forceGraph: ForceGraph = if (settings.value!!.artistsSelected) {
                Helper.prepareGraphArtists(
                    settings.value!!,
                    recommendedTracks,
                    topArtists.value!!,
                    context!!
                )
            } else {
                Helper.prepareGraphTracks(
                    settings.value!!,
                    recommendedTracksFeatures,
                    topTracks.value!!.toMutableList(),
                    context!!
                )
            }
            if (Config.trimMetrics) {
                val trimmedNodes = trimNodes(forceGraph.nodes, forceGraph.links)
                metricNodes.value = ArrayList(trimmedNodes)
                metricLinks.value = ArrayList(trimEdges(forceGraph.links, trimmedNodes))
            } else {
                metricNodes.value = forceGraph.nodes
                metricLinks.value = forceGraph.links
            }
        }
    }
}