package com.example.spotifyexplained.ui.recommend.spotify.tracks

import android.app.Activity
import android.content.Context
import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.Observable
import androidx.lifecycle.*
import com.example.spotifyexplained.activity.MainActivity
import com.example.spotifyexplained.database.TrackRecommendEntity
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
    val settings = MutableLiveData<GraphSettings>().apply { value = GraphSettings(
        artistsSelected = false,
        relatedFlag = false,
        genresFlag = true,
        featuresFlag = true,
        zoomFlag = true
    ) }

    val nodes : MutableLiveData<ArrayList<D3Node>> by lazy {
        MutableLiveData<ArrayList<D3Node>>(arrayListOf())
    }
    val links : MutableLiveData<ArrayList<D3Link>> by lazy {
        MutableLiveData<ArrayList<D3Link>>(arrayListOf())
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

    private val recommendedTracks: MutableLiveData<List<Track>> by lazy {
        MutableLiveData<List<Track>>(arrayListOf())
    }

    private val recommendedTracksFeatures: MutableLiveData<List<TrackAudioFeatures>> by lazy {
        MutableLiveData<List<TrackAudioFeatures>>(arrayListOf())
    }

    private val userTracks: MutableLiveData<List<TrackAudioFeatures>> by lazy {
        MutableLiveData<List<TrackAudioFeatures>>(mutableListOf())
    }

    private val topArtists: MutableLiveData<MutableList<Artist>> by lazy {
        MutableLiveData<MutableList<Artist>>(mutableListOf())
    }

    private val context: Context? by lazy {
        activity
    }

    init {
        if (SessionManager.tokenExpired()) {
            (activity as MainActivity).authorizeUser()
        } else {
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
                userTracks.value = (context as MainActivity).viewModel.topTracks.value
                recommendBasedOnTracks()
                recommendTracksGetFeatures()
                getRecommendedTracksArtistGenres()
                getRecommendedTracksRelatedArtists()
                saveRecommendedTracksToDatabase()
            }
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
        if (SessionManager.tokenExpired()) {
            (context as MainActivity).authorizeUser()
        } else {
            viewModelScope.launch {
                loadingState.value = LoadingState.LOADING
                recommendedTracks.value = tracks
                recommendTracksGetFeatures()
                topArtists.value = (context as MainActivity).viewModel.topArtists.value
                userTracks.value = (context as MainActivity).viewModel.topTracks.value
                prepareNodesAndEdges()
                loadingState.value = LoadingState.SUCCESS
            }
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
            ApiHelper.getArtistWithGenres(recommendedTracks.value!!.toMutableList()) ?: return
        for (i in response.artists.indices) {
            recommendedTracks.value!![i].trackGenres = response.artists[i].genres
            recommendedTracks.value!![i].artists[0].genres = response.artists[i].genres
            recommendedTracks.value!![i].artists[0].artistPopularity =
                response.artists[i].artistPopularity
            recommendedTracks.value!![i].artists[0].images = response.artists[i].images
        }
    }

    /**
     * Assigns related artist to each recommended track's artist
     */
    private suspend fun getRecommendedTracksRelatedArtists() {
        for (track in recommendedTracks.value!!) {
            val relatedArtists = ApiHelper.getRelatedArtists(track.artists[0].artistId)
            track.artists[0].related_artists = relatedArtists?.toList() ?: mutableListOf()
        }
    }

    /**
     * Calls repository to save tracks to database
     */
    private fun saveRecommendedTracksToDatabase() {
        insertTracksBasedOnTracks(recommendedTracks.value!!)
    }

    /**
     *  Gets recommended tracks
     */
    private suspend fun recommendBasedOnTracks() {
        val response =
            ApiHelper.getRecommendsBasedOnTracks(userTracks.value!!.take(5).map { it.track }.toMutableList())
                ?: return
        recommendedTracks.value = response.tracks.toMutableList()
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
     * Gathers all graph elements
     */
    private suspend fun prepareNodesAndEdges(){
        val forceGraph : ForceGraph = if (settings.value!!.artistsSelected){
            Helper.prepareGraphArtists(settings.value!!, recommendedTracks.value!!, topArtists.value!!)
        } else {
            Helper.prepareGraphTracks(settings.value!!, recommendedTracksFeatures.value!!, userTracks.value!!.toMutableList())
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
}