package com.example.spotifyexplained.ui.recommend.spotify.combined

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import com.example.spotifyexplained.activity.MainActivity
import com.example.spotifyexplained.database.CombinedRecommendEntity
import com.example.spotifyexplained.general.Helper
import com.example.spotifyexplained.general.VisualTabClickHandler
import com.example.spotifyexplained.model.*
import com.example.spotifyexplained.services.ApiHelper
import com.example.spotifyexplained.repository.TrackRepository
import com.example.spotifyexplained.services.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
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
    val settings = MutableLiveData<GraphSettings>().apply { value = GraphSettings(
        artistsSelected = false,
        relatedFlag = false,
        genresFlag = true,
        featuresFlag = true,
        zoomFlag = true
    ) }

    val nodes: MutableLiveData<ArrayList<D3Node>> by lazy {
        MutableLiveData<ArrayList<D3Node>>(arrayListOf())
    }
    val links: MutableLiveData<ArrayList<D3Link>> by lazy {
        MutableLiveData<ArrayList<D3Link>>(arrayListOf())
    }
    val allGraphArtistNodes = MutableLiveData<MutableList<Artist>>().apply{ mutableListOf<Artist>() }
    val allGraphTrackNodes = MutableLiveData<MutableList<TrackAudioFeatures>>().apply{ mutableListOf<TrackAudioFeatures>() }

    val topTracks: MutableLiveData<List<TrackAudioFeatures>> by lazy { MutableLiveData<List<TrackAudioFeatures>>(mutableListOf()) }
    val topArtists: MutableLiveData<MutableList<Artist>> by lazy {
        MutableLiveData<MutableList<Artist>>(
            mutableListOf()
        )
    }
    val selectedIds: MutableLiveData<MutableList<Pair<RecommendSeedType, String>>> by lazy {
        MutableLiveData<MutableList<Pair<RecommendSeedType, String>>>(
            mutableListOf()
        )
    }
    private val recommendedTracks: MutableLiveData<List<Track>> by lazy {
        MutableLiveData<List<Track>>(
            arrayListOf()
        )
    }
    private val recommendedTracksFeatures: MutableLiveData<List<TrackAudioFeatures>> by lazy {
        MutableLiveData<List<TrackAudioFeatures>>(arrayListOf())
    }
    val availableGenres: MutableLiveData<List<String>> by lazy {
        MutableLiveData<List<String>>(
            listOf()
        )
    }
    var genreColorMap: HashMap<String, MixableColor> = HashMap()
    private val tracksFromDatabaseFlow: Flow<MutableList<CombinedRecommendEntity>> =
        repository.allCombinedTracks
    val tracksFromDatabaseLiveData: LiveData<MutableList<CombinedRecommendEntity>> =
        repository.allCombinedTracks.asLiveData()

    private fun insertTracksBasedOnCombined(tracks: List<Track>) = viewModelScope.launch {
        repository.insertCombined(tracks.map { CombinedRecommendEntity(it.trackId, it) })
    }

    private val context: Context? by lazy {
        activity
    }

    init {
        if (SessionManager.tokenExpired()) {
            (activity as MainActivity).authorizeUser()
        } else {
            viewModelScope.launch {
                topArtists.value = (context as MainActivity).viewModel.topArtists.value
                topTracks.value = (context as MainActivity).viewModel.topTracks.value
                getAvailableGenreSeeds()
                loadingState.value = LoadingState.SETTINGS_LOADED
                tracksFromDatabaseFlow.collect { trackEntities ->
                    if (trackEntities.isNotEmpty()) {
                        prepareGraph(trackEntities.map { it.track }.toMutableList())
                    }
                }
            }
        }
    }
    /**
     * Prepares full recommended tracks data
     */
    fun reload() {
        if (SessionManager.tokenExpired()) {
            (context as MainActivity).authorizeUser()
        } else {
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
                loadingState.value = LoadingState.SUCCESS
            }
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
        if (SessionManager.tokenExpired()) {
            (context as MainActivity).authorizeUser()
        } else {
            viewModelScope.launch {
                loadingState.value = LoadingState.LOADING
                recommendedTracks.value = tracks
                recommendTracksGetFeatures()
                topArtists.value = (context as MainActivity).viewModel.topArtists.value
                Log.e("prepare", "--- Prepare Nodes and Edges ---")
                prepareNodesAndEdges()
                loadingState.value = LoadingState.SUCCESS
            }
        }
    }
    /**
     *  Gets recommended tracks
     */
    private suspend fun recommendTracksGetFeatures() {
        val response =
            ApiHelper.getTracksAudioFeatures(recommendedTracks.value!!)
                ?: return
        recommendedTracksFeatures.value = response.toMutableList()
    }

    /**
     * Assigns artist data to each recommended track
     */
    private suspend fun getRecommendedTracksArtistGenres() {
        val chunkedTracks = recommendedTracks.value!!.chunked(50)
        var offset = 0
        for (chunk in chunkedTracks) {
            val response = ApiHelper.getArtistWithGenres(chunk.toMutableList()) ?: return
            for (i in response.artists.indices) {
                val index = i + offset
                recommendedTracks.value!![index].trackGenres = response.artists[i].genres
                recommendedTracks.value!![index].artists[0].genres = response.artists[i].genres
                recommendedTracks.value!![index].artists[0].artistPopularity =
                    response.artists[i].artistPopularity
                recommendedTracks.value!![index].artists[0].images = response.artists[i].images
            }
            offset += response.artists.size
        }
    }

    /**
     * Gathers all graph elements
     */
    private suspend fun prepareNodesAndEdges(){
        val forceGraph : ForceGraph = if (settings.value!!.artistsSelected){
            Helper.prepareGraphArtists(settings.value!!, recommendedTracks.value!!, topArtists.value!!)
        } else {
            Helper.prepareGraphTracks(settings.value!!, recommendedTracksFeatures.value!!, topTracks.value!!.toMutableList())
        }
        nodes.value = forceGraph.nodes
        genreColorMap = forceGraph.genreColorMap
        allGraphArtistNodes.value = forceGraph.nodeArtists.toMutableList()
        if (!settings.value!!.artistsSelected) {
            allGraphTrackNodes.value =
                ApiHelper.getTracksAudioFeatures(forceGraph.nodeTracks)?.toMutableList() ?: return
        }
        links.value = forceGraph.links
    }

    /**
     * Assigns related artist to each recommended track's artist
     */
    private suspend fun getRecommendedTracksRelatedArtists() {
        for (track in recommendedTracks.value!!) {
            val trackArtist = (context as MainActivity).viewModel.topArtists.value!!.firstOrNull { it.artistId == track.artists[0].artistId }
            track.artists[0].related_artists = trackArtist?.related_artists
                ?: (ApiHelper.getRelatedArtists(track.artists[0].artistId)?.toList()
                    ?: mutableListOf())
        }
    }

    private suspend fun getAvailableGenreSeeds() {
        val availableGenreSeeds = ApiHelper.getAvailableGenreSeeds() ?: GenreSeeds(arrayOf())
        availableGenres.value = availableGenreSeeds.seeds.toList()
    }
    /**
     * Calls repository to save tracks to database
     */
    private fun saveRecommendedTracksToDatabase() {
        insertTracksBasedOnCombined(recommendedTracks.value!!)
    }
    /**
     *  Gets recommended tracks
     */
    private suspend fun recommendBasedOnTracks() {
        val seedsChunked = selectedIds.value!!.chunked(5).toMutableList()
        val allRecommendedTracks = mutableListOf<Track>()
        for (chunk in seedsChunked) {
            val response = ApiHelper.getRecommendsCombined(chunk.toMutableList()) ?: return
            allRecommendedTracks.addAll(response.tracks)
        }
        recommendedTracks.value = allRecommendedTracks.distinct()
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
        return Helper.showBundleDetailInfo(nodeIndices, currentIndex, allGraphArtistNodes.value!!, allGraphTrackNodes.value!!.map { it.track }, recommendedTracks.value!!, genreColorMap)
    }
    /**
     * Prepares data for line detail window
     * @param [nodeIndices] indices of the nodes contained in the bundle; indices are separated by ','.
     */
    fun showLineDetailInfo(nodeIndices: String?) {
        detailViewVisible.value = true
        when (nodeIndices!!.split(",").last()) {
            "RELATED" -> {
                lineInfo.value = Helper.showLineDetailInfo(nodeIndices, allGraphArtistNodes.value!!, allGraphTrackNodes.value!!.map { it.track })
                detailVisibleType.value = DetailVisibleType.LINE
            }
            "GENRE" -> {
                lineGenreInfo.value = Helper.showLineDetailGenreInfo(nodeIndices, allGraphArtistNodes.value!!, allGraphTrackNodes.value!!.map { it.track })
                detailVisibleType.value = DetailVisibleType.LINEGENRE
            }
            "FEATURE" -> {
                lineFeaturesInfo.value = Helper.showLineDetailFeatureInfo(nodeIndices, allGraphTrackNodes.value!!)
                detailVisibleType.value = DetailVisibleType.LINEFEATURE
            }
            "BUNDLE" -> {
                lineBundleInfo.value = if (settings.value!!.artistsSelected) {
                    Helper.showBundleLineInfoArtists(nodeIndices, allGraphArtistNodes.value!!, genreColorMap)
                } else {
                    Helper.showBundleLineInfoTracks(nodeIndices, allGraphTrackNodes.value!!, genreColorMap)
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

    /**
     *  Unused functions for graph generation for experiment
     * **/
    private fun trimArtists(
        nodeArtists: List<Artist>,
        edgesList: MutableList<Pair<Artist, Artist>>
    ): List<Artist> {
        val nodesWithDegrees = mutableMapOf<Artist, Int>()
        for (artist in nodeArtists) {
            nodesWithDegrees[artist] =
                edgesList.filter { it.first == artist || it.second == artist }.count()
        }
        val sorted = nodesWithDegrees.toList().sortedByDescending { (_, value) -> value }.map { it.first }
            .dropLast(20)
        return sorted.asSequence().shuffled().take(50).toList()
    }

    private fun trimEdges(
        edgesList: MutableList<Pair<Artist, Artist>>,
        nodeArtists: List<Artist>
    ): MutableList<Pair<Artist, Artist>> {
        val result =
            edgesList.filter { nodeArtists.contains(it.first) && nodeArtists.contains(it.second) }
        return result.toMutableList()
    }
}