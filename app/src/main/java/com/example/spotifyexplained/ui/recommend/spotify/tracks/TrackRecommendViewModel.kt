package com.example.spotifyexplained.ui.recommend.spotify.tracks

import android.app.Activity
import android.content.Context
import androidx.lifecycle.*
import com.example.spotifyexplained.activity.MainActivity
import com.example.spotifyexplained.database.TrackRecommendEntity
import com.example.spotifyexplained.general.Config
import com.example.spotifyexplained.general.GraphInfoHelper
import com.example.spotifyexplained.general.Helper
import com.example.spotifyexplained.general.VisualTabClickHandler
import com.example.spotifyexplained.model.*
import com.example.spotifyexplained.model.enums.*
import com.example.spotifyexplained.services.ApiHelper
import com.example.spotifyexplained.repository.TrackRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * ViewModel for recommendations from Spotify based on track seed
 * @param [activity] reference to MainActivity
 * @param [repository] repository for database
 */
class TrackRecommendViewModel(activity: Activity, private val repository: TrackRepository) : ViewModel(),
    VisualTabClickHandler {
    val visualState = MutableLiveData<VisualState>().apply { value = VisualState.TABLE }
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
    val imageUrl = MutableLiveData<String>().apply { value = "" }
    val settings = MutableLiveData<GraphSettings>().apply { value = Config.graphSettings}

    val nodes : MutableLiveData<ArrayList<D3ForceNode>> by lazy {
        MutableLiveData<ArrayList<D3ForceNode>>(arrayListOf())
    }
    val links : MutableLiveData<ArrayList<D3ForceLink>> by lazy {
        MutableLiveData<ArrayList<D3ForceLink>>(arrayListOf())
    }
    val allGraphArtistNodes = MutableLiveData<MutableList<Artist>>().apply{ mutableListOf<Artist>() }
    val allGraphTrackNodes = MutableLiveData<MutableList<TrackAudioFeatures>>().apply{ mutableListOf<TrackAudioFeatures>() }

    val tracksFromDatabaseLiveData: LiveData<MutableList<TrackRecommendEntity>> =
        repository.allTrackTracks.asLiveData()

    private val tracksFromDatabaseFlow: Flow<MutableList<TrackRecommendEntity>> = repository.allTrackTracks

    private fun insertTracksBasedOnTracks(tracks: List<Track>) = viewModelScope.launch {
        repository.insertTracks(tracks.map { TrackRecommendEntity(it.trackId, it) })
    }

    var genreColorMap: HashMap<String, MixableColor> = HashMap()

    private lateinit var recommendedTracks: List<Track>
    private lateinit var recommendedTracksFeatures: List<TrackAudioFeatures>
    private lateinit var userTracks: List<TrackAudioFeatures>
    private lateinit var topArtists: List<Artist>

    private val context: Context? by lazy {
        activity
    }

    init {
        viewModelScope.launch {
            tracksFromDatabaseFlow.collect { trackEntities ->
                if (trackEntities.isEmpty()) {
                    reload()
                } else {
                    prepareGraph(trackEntities.map { it.track }.toList())
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
            topArtists = (context as MainActivity).viewModel.topArtists.value!!
            userTracks = (context as MainActivity).viewModel.topTracks.value!!
            recommendBasedOnTracks()
            recommendTracksGetFeatures()
            getRecommendedTracksArtistGenres()
            getRecommendedTracksRelatedArtists()
            saveRecommendedTracksToDatabase()
        }
    }

    /**
     * Clears database table
     */
    fun clearData() {
        viewModelScope.launch {
            repository.deleteTrack()
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
            topArtists = (context as MainActivity).viewModel.topArtists.value!!
            userTracks = (context as MainActivity).viewModel.topTracks.value!!
            prepareNodesAndEdges()
            loadingState.value = LoadingState.SUCCESS
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
     * Assigns artist data to each recommended track
     */
    private suspend fun getRecommendedTracksArtistGenres() {
        val response =
            ApiHelper.getArtistWithGenres(recommendedTracks.toMutableList(), context!!) ?: return
        for (i in response.artists.indices) {
            recommendedTracks[i].trackGenres = response.artists[i].genres
            recommendedTracks[i].artists[0].genres = response.artists[i].genres
            recommendedTracks[i].artists[0].artistPopularity = response.artists[i].artistPopularity
            recommendedTracks[i].artists[0].images = response.artists[i].images
        }
    }

    /**
     * Assigns related artist to each recommended track's artist
     */
    private suspend fun getRecommendedTracksRelatedArtists() {
        for (track in recommendedTracks) {
            val relatedArtists = ApiHelper.getRelatedArtists(track.artists[0].artistId, context!!)
            track.artists[0].related_artists = relatedArtists?.toList() ?: mutableListOf()
        }
    }

    /**
     * Calls repository to save tracks to database
     */
    private fun saveRecommendedTracksToDatabase() {
        insertTracksBasedOnTracks(recommendedTracks)
    }

    /**
     *  Gets recommended tracks
     */
    private suspend fun recommendBasedOnTracks() {
        val response =
            ApiHelper.getRecommendsBasedOnTracks(userTracks.take(5).map { it.track }.toMutableList(), context!!)
                ?: return
        recommendedTracks = response.tracks.toMutableList()
    }

    /**
     *  Gets recommended tracks
     */
    private suspend fun recommendTracksGetFeatures() {
        recommendedTracksFeatures = ApiHelper.getTracksAudioFeatures(recommendedTracks, context!!) ?: listOf()
    }

    /**
     * Gathers all graph elements
     */
    private suspend fun prepareNodesAndEdges(){
        val forceGraph : ForceGraph = if (settings.value!!.artistsSelected){
            Helper.prepareGraphArtists(settings.value!!, recommendedTracks, topArtists, context!!)
        } else {
            Helper.prepareGraphTracks(settings.value!!, recommendedTracksFeatures, userTracks.toMutableList(), context!!)
        }
        nodes.value = forceGraph.nodes
        genreColorMap = forceGraph.genreColorMap
        allGraphArtistNodes.value = forceGraph.nodeArtists.toMutableList()
        if (!settings.value!!.artistsSelected) {
            allGraphTrackNodes.value =
                ApiHelper.getTracksAudioFeatures(forceGraph.nodeTracks, context!!)?.toMutableList() ?: return
        }
        links.value = forceGraph.links
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
        return GraphInfoHelper.showBundleDetailInfo(nodeIndices, currentIndex, allGraphArtistNodes.value!!, allGraphTrackNodes.value!!.map { it.track }, recommendedTracks, genreColorMap)
    }
    /**
     * Prepares data for line detail window
     * @param [nodeIndices] indices of the nodes contained in the bundle; indices are separated by ','.
     */
    fun showLineDetailInfo(nodeIndices: String?) {
        detailViewVisible.value = true
        when (nodeIndices!!.split(",").last()) {
            "RELATED" -> {
                lineInfo.value = GraphInfoHelper.showLineDetailInfo(nodeIndices, allGraphArtistNodes.value!!, allGraphTrackNodes.value!!.map { it.track })
                detailVisibleType.value = DetailVisibleType.LINE
            }
            "GENRE" -> {
                lineGenreInfo.value = GraphInfoHelper.showLineDetailGenreInfo(nodeIndices, allGraphArtistNodes.value!!, allGraphTrackNodes.value!!.map { it.track })
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
}