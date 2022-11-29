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
 * ViewModel for user's top artists from Spotify
 * @param [activity] reference to MainActivity
 */
class TopArtistsViewModel(activity: Activity) : ViewModel(), VisualTabClickHandler {
    val visualState = MutableLiveData<VisualState>().apply { value = VisualState.TABLE }
    val loadingState = MutableLiveData<LoadingState>().apply { value = LoadingState.LOADING }
    val graphLoadingState = MutableLiveData<LoadingState>().apply { value = LoadingState.LOADING }
    val detailViewVisible = MutableLiveData<Boolean>().apply { value = false }
    val detailVisibleType = MutableLiveData<DetailVisibleType>()
    val lineInfo = MutableLiveData<LineDetailInfo>()
    val lineGenreInfo = MutableLiveData<LineDetailGenreInfo>()
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

    val artists: MutableLiveData<List<Artist>> by lazy {
        MutableLiveData<List<Artist>>(ArrayList())
    }
    var genreColorMap: HashMap<String, MixableColor> = HashMap()

    val allGraphNodes: MutableLiveData<MutableList<Artist>> by lazy {
        MutableLiveData<MutableList<Artist>>(mutableListOf())
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
                getUserTopArtists()
                getArtistsRelatedArtists()
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
     * Gathers all graph elements
     */
    private suspend fun prepareNodesAndEdges(){
        val forceGraph : ForceGraph = Helper.prepareGraphTopArtists(settings.value!!, artists.value!!.toMutableList())
        nodes.value = forceGraph.nodes
        genreColorMap = forceGraph.genreColorMap
        allGraphNodes.value = forceGraph.nodeArtists.toMutableList()
        links.value = forceGraph.links
    }

    private suspend fun getUserTopArtists(){
        artists.value = ApiHelper.getUserTopArtists(50)
    }

    /**
     * Assigns related artist to each track's artist
     */
    private suspend fun getArtistsRelatedArtists() {
        for (artist in artists.value!!) {
            val savedArtist = (context as MainActivity).viewModel.topArtists.value!!.firstOrNull { it.artistId == artist.artistId }
            artist.related_artists = savedArtist?.related_artists
                ?: (ApiHelper.getRelatedArtists(artist.artistId)?.toList() ?: mutableListOf())
        }
    }

    /**
     * Prepares data for graph detail window
     * @param [selectedArtist] selected node
     * @return list of genres with colors
     */
    fun showDetailInfo(selectedArtist: Artist): List<GenreColor>? {
        artistName.value = selectedArtist.artistName
        imageUrl.value =
            if (selectedArtist.images!!.isNotEmpty()) selectedArtist.images!![0].url else ""
        detailViewVisible.value = true
        detailVisibleType.value = DetailVisibleType.SINGLE
        return Helper.getGenreColorList(selectedArtist, genreColorMap)
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
        return Helper.showBundleDetailInfo(nodeIndices, currentIndex, allGraphNodes.value!!, listOf(), listOf(), genreColorMap)
    }
    /**
     * Prepares data for line detail window
     * @param [nodeIndices] indices of the nodes contained in the bundle; indices are separated by ','.
     */
    fun showLineDetailInfo(nodeIndices: String?) {
        detailViewVisible.value = true
        when (nodeIndices!!.split(",").last()) {
            "RELATED" -> {
                lineInfo.value = Helper.showLineDetailInfo(nodeIndices, allGraphNodes.value!!, listOf())
                detailVisibleType.value = DetailVisibleType.LINE
            }
            "GENRE" -> {
                lineGenreInfo.value = Helper.showLineDetailGenreInfo(nodeIndices, allGraphNodes.value!!, listOf())
                detailVisibleType.value = DetailVisibleType.LINEGENRE
            }
            "BUNDLE" -> {
                lineBundleInfo.value = Helper.showBundleLineInfoArtists(nodeIndices, allGraphNodes.value!!, genreColorMap)
                detailVisibleType.value = DetailVisibleType.LINEBUNDLE
            }
        }
    }

    override fun onListClick() { visualState.value = VisualState.TABLE }
    override fun onGraphClick() { visualState.value = VisualState.GRAPH }
    override fun onSettingsClick() { visualState.value = VisualState.SETTINGS }
}