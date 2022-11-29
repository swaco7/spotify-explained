package com.example.spotifyexplained.ui.topItems

import android.app.Activity
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spotifyexplained.activity.MainActivity
import com.example.spotifyexplained.general.Helper
import com.example.spotifyexplained.general.VisualTabClickHandler
import com.example.spotifyexplained.model.*
import com.example.spotifyexplained.services.ApiHelper
import com.example.spotifyexplained.services.SessionManager
import kotlinx.coroutines.launch

/**
 * ViewModel for user's top tracks by Spotify
 * @param [activity] reference to MainActivity
 */
class TopTracksViewModel(activity: Activity) : ViewModel(), VisualTabClickHandler {
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

    val tracks: MutableLiveData<List<Track>> by lazy {
        MutableLiveData<List<Track>>(ArrayList())
    }
    val tracksFeatures: MutableLiveData<List<TrackAudioFeatures>> by lazy {
        MutableLiveData<List<TrackAudioFeatures>>(ArrayList())
    }
    var genreColorMap: HashMap<String, MixableColor> = HashMap()

    val allGraphNodes: MutableLiveData<MutableList<TrackAudioFeatures>> by lazy {
        MutableLiveData<MutableList<TrackAudioFeatures>>(mutableListOf())
    }
    val nodes: MutableLiveData<ArrayList<D3Node>> by lazy {
        MutableLiveData<ArrayList<D3Node>>(ArrayList())
    }
    val links: MutableLiveData<ArrayList<D3Link>> by lazy {
        MutableLiveData<ArrayList<D3Link>>(ArrayList())
    }

    private val context: Context? by lazy {
        activity
    }

    init {
        if (SessionManager.tokenExpired()){
            (activity as MainActivity).authorizeUser()
        }  else {
            viewModelScope.launch {
                loadingState.value = LoadingState.LOADING
                getUserTopTracks()
                recommendTracksGetFeatures()
                getTracksArtistGenres()
                getTracksRelatedArtists()
                prepareNodesAndEdges()
                loadingState.value = LoadingState.SUCCESS
            }
        }
    }

    private suspend fun getUserTopTracks(){
        tracks.value = ApiHelper.getUserTopTracks(50)
    }

    /**
     *  Gets recommended tracks
     */
    private suspend fun recommendTracksGetFeatures() {
        val response =
            ApiHelper.getTracksAudioFeatures(tracks.value!!)
                ?: return
        tracksFeatures.value = response.toMutableList()
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
     * Gathers all graph elements
     */
    private suspend fun prepareNodesAndEdges(){
        val forceGraph = Helper.prepareGraphTracks(settings.value!!, tracksFeatures.value!!, tracksFeatures.value!!.toMutableList())
        nodes.value = forceGraph.nodes
        genreColorMap = forceGraph.genreColorMap
        allGraphNodes.value = ApiHelper.getTracksAudioFeatures(forceGraph.nodeTracks)?.toMutableList() ?: return
        links.value = forceGraph.links
    }

    /**
     * Assigns artist data to each recommended track
     */
    private suspend fun getTracksArtistGenres() {
        val response = ApiHelper.getArtistWithGenres(tracks.value!!.toMutableList()) ?: return
        for (i in response.artists.indices) {
            tracks.value!![i].trackGenres = response.artists[i].genres
            tracks.value!![i].artists[0].genres = response.artists[i].genres
            tracks.value!![i].artists[0].artistPopularity = response.artists[i].artistPopularity
            tracks.value!![i].artists[0].images = response.artists[i].images
        }
    }
    /**
     * Assigns related artist to each track's artist
     */
    private suspend fun getTracksRelatedArtists() {
        for (track in tracks.value!!) {
            var trackArtist = (context as MainActivity).viewModel.topArtists.value!!.firstOrNull { it.artistId == track.artists[0].artistId }
            if (trackArtist == null){
                trackArtist = (context as MainActivity).viewModel.topTracks.value!!.firstOrNull { it.track.artists[0].artistId == track.artists[0].artistId }?.track?.artists?.get(0)
            }
            track.artists[0].related_artists = trackArtist?.related_artists
                ?: (ApiHelper.getRelatedArtists(track.artists[0].artistId)?.toList() ?: mutableListOf())
        }
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
        return Helper.showBundleDetailInfo(nodeIndices, currentIndex, listOf(), allGraphNodes.value!!.map { it.track }, tracksFeatures.value!!.map { it.track }, genreColorMap)
    }
    /**
     * Prepares data for line detail window
     * @param [nodeIndices] indices of the nodes contained in the bundle; indices are separated by ','.
     */
    fun showLineDetailInfo(nodeIndices: String?) {
        detailViewVisible.value = true
        when (nodeIndices!!.split(",").last()) {
            "RELATED" -> {
                lineInfo.value = Helper.showLineDetailInfo(nodeIndices, listOf(), allGraphNodes.value!!.map { it.track })
                detailVisibleType.value = DetailVisibleType.LINE
            }
            "GENRE" -> {
                lineGenreInfo.value = Helper.showLineDetailGenreInfo(nodeIndices, listOf(), allGraphNodes.value!!.map { it.track })
                detailVisibleType.value = DetailVisibleType.LINEGENRE
            }
            "FEATURE" -> {
                lineFeaturesInfo.value = Helper.showLineDetailFeatureInfo(nodeIndices, allGraphNodes.value!!)
                detailVisibleType.value = DetailVisibleType.LINEFEATURE
            }
            "BUNDLE" -> {
                lineBundleInfo.value = Helper.showBundleLineInfoTracks(nodeIndices, allGraphNodes.value!!, genreColorMap)
                detailVisibleType.value = DetailVisibleType.LINEBUNDLE
            }
        }
    }

    override fun onListClick() { visualState.value = VisualState.TABLE }
    override fun onGraphClick() { visualState.value = VisualState.GRAPH }
    override fun onSettingsClick() { visualState.value = VisualState.SETTINGS }
}