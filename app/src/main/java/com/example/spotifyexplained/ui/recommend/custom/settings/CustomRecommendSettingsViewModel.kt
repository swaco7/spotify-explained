package com.example.spotifyexplained.ui.recommend.custom.settings

import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.*
import com.example.spotifyexplained.R
import com.example.spotifyexplained.activity.MainActivity
import com.example.spotifyexplained.database.entity.TrackCustomEntity
import com.example.spotifyexplained.database.entity.TrackInPoolEntity
import com.example.spotifyexplained.general.Config
import com.example.spotifyexplained.general.ExpandClickHandler
import com.example.spotifyexplained.general.Helper
import com.example.spotifyexplained.general.VisualTabClickHandler
import com.example.spotifyexplained.model.*
import com.example.spotifyexplained.model.enums.*
import com.example.spotifyexplained.repository.TrackRepository
import com.example.spotifyexplained.services.ApiRepository
import com.example.spotifyexplained.ui.general.HelpDialogFragment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * ViewModel for recommendations from custom recommender system based on similarity with user's tracks based on settings
 * @param [activity] reference to MainActivity
 * @param [repository] repository for database
 */
@RequiresApi(Build.VERSION_CODES.N)
class CustomRecommendSettingsViewModel(activity: Activity, private val repository: TrackRepository) : ViewModel(), VisualTabClickHandler, ExpandClickHandler {
    val visualState = MutableLiveData<VisualState>().apply { value = VisualState.TABLE }
    val loadingState = MutableLiveData<LoadingState>().apply { value = LoadingState.LOADING }
    val graphLoadingState = MutableLiveData<LoadingState>().apply { value = LoadingState.LOADING }
    val detailViewVisible = MutableLiveData<Boolean>().apply { value = false }
    val detailVisibleType = MutableLiveData<DetailVisibleType>()
    val zoomType = MutableLiveData<ZoomType>().apply { value = ZoomType.RESPONSIVE }
    val isTrack = MutableLiveData<Boolean>().apply { value = false }
    val selectedTrack = MutableLiveData<Track>()
    val artistName = MutableLiveData<String>().apply { value = "" }
    val imageUrl = MutableLiveData<String>().apply { value = "" }
    val tabVisible = MutableLiveData<Boolean>().apply { value = false }
    val expanded = MutableLiveData<Boolean>().apply { value = false }
    val popularityCheck = MutableLiveData<Boolean>().apply{ value = false }
    val metricIndex = MutableLiveData<Int>().apply { value = 0 }

    val allGraphNodes: MutableLiveData<MutableList<BundleTrackFeatureItem>> by lazy {
        MutableLiveData<MutableList<BundleTrackFeatureItem>>(mutableListOf())
    }
    val audioFeatures: MutableLiveData<List<AudioFeatures>> by lazy {
        MutableLiveData<List<AudioFeatures>>(arrayListOf())
    }
    val nodes: MutableLiveData<ArrayList<D3ForceNode>> by lazy {
        MutableLiveData<ArrayList<D3ForceNode>>(arrayListOf())
    }
    val linksDistance: MutableLiveData<ArrayList<D3ForceLinkDistance>> by lazy {
        MutableLiveData<ArrayList<D3ForceLinkDistance>>(arrayListOf())
    }

    val featuresList = MutableLiveData<MutableList<AudioFeature>>().apply {
        value = mutableListOf()
        for (feature in AudioFeatureType.values()){
            value!!.add(AudioFeature(feature.value, 1.0))
        }
    }
    val userTracksAudioFeatures: MutableLiveData<List<TrackAudioFeatures>> by lazy {
        MutableLiveData<List<TrackAudioFeatures>>(ArrayList())
    }
    val recommendedTracks: LiveData<MutableList<TrackCustomEntity>> = repository.allTracksCustom.asLiveData()

    private val tracksCount = MutableLiveData<Int>().apply {
        value = 5
    }
    private val tracksFromDatabaseFlow: Flow<MutableList<TrackCustomEntity>> = repository.allTracksCustom
    private val tracksPoolFromDatabaseFlow : Flow<MutableList<TrackInPoolEntity>> = repository.allTracksPool

    private lateinit var recommendedTracksLocal: List<TrackCustomEntity>

    private fun insertAll(tracks: List<TrackCustomEntity>) = viewModelScope.launch {
        repository.insertCustomAll(tracks)
    }

    fun setTracksCount(count : Float) {
        tracksCount.value = count.toInt()
    }

    fun setFeatureValue(at: Int, value: Double) {
        featuresList.value!![at].value = value
    }

    private val context: Context? by lazy {
        activity
    }

    init {
        viewModelScope.launch {
            tracksFromDatabaseFlow.collect { trackEntities ->
                if (trackEntities.isEmpty()) {
                    reloadSpecific()
                } else {
                    prepareGraph(trackEntities.map { it }.toMutableList())
                }
            }
        }
    }

    /**
     * Prepares full recommended tracks data
     */
    private fun reloadSpecific() {
        viewModelScope.launch {
            loadingState.value = LoadingState.LOADING
            getUserTracks()
            tracksPoolFromDatabaseFlow.collect { tracks ->
                val recommendedPool = tracks.filter { it.trackType == 0 }
                    .map { TrackAudioFeatures(it.track, it.features) }.toMutableList()
                val usersTracksPool = tracks.filter { it.trackType == 1 }
                    .map { TrackAudioFeatures(it.track, it.features) }.toMutableList()
                if (tracks.isNotEmpty()) {
                    getMostSimilarTracksBySettings(recommendedPool, usersTracksPool)
                    loadingState.value = LoadingState.SUCCESS
                }
                loadingState.value = LoadingState.RELOADED
            }
        }
    }

    fun clearSpecificData() {
        viewModelScope.launch {
            repository.deleteCustom()
        }
    }

    fun infoClicked(){
        val dialog = HelpDialogFragment((context as MainActivity).resources.getString(R.string.settings_features_info_text))
        val fragmentManager = (context as MainActivity).supportFragmentManager
        dialog.show(fragmentManager, "help")
    }

    /**
     * Prepares graph data from received tracks
     * @param [tracks] tracks for graph generation
     */
    private fun prepareGraph(tracks: MutableList<TrackCustomEntity>) {
        viewModelScope.launch {
            recommendedTracksLocal = tracks
            getUserTracks()
            getAllRelatedArtist()
            prepareD3RelationsDistance()
            loadingState.value = LoadingState.SUCCESS
        }
    }

    private suspend fun getUserTracks() {
        val topTracks = ApiRepository.getUserTopTracks(50, context as MainActivity) ?: mutableListOf()
        val savedTracks = ApiRepository.getUserSavedTracks(Config.savedTracksLimit, context!!) ?: mutableListOf()
        val userTracks =  (savedTracks + topTracks).distinctBy { it.trackId }
        userTracksAudioFeatures.value = ApiRepository.getTracksAudioFeatures(userTracks.toMutableList(), context!!)
    }

    /**
     * Assigns artist data to each recommended track
     */
    private suspend fun getArtistGenres(tracks: List<TrackAudioFeatures>) : List<TrackAudioFeatures>? {
        val response = ApiRepository.getArtistWithGenres(tracks.map { it.track }.toMutableList(), context!!) ?: return null
        for (i in response.artists.indices) {
            tracks[i].track.trackGenres = response.artists[i].genres
            tracks[i].track.artists[0].artistPopularity = response.artists[i].artistPopularity
            tracks[i].track.artists[0].genres = response.artists[i].genres
        }
        return tracks
    }

    private suspend fun getAllRelatedArtist(){
        for (track in recommendedTracksLocal) {
            val artistId = track.track.artists[0].artistId
            val relatedArtists = ApiRepository.getRelatedArtists(artistId, context!!)
            track.track.track_related_artists = relatedArtists?.toList() ?: mutableListOf()
        }
    }

    /**
     * Finds most similar tracks from the pool to specific number of user's tracks with specific audio features weights, and pushes them to the database repository
     * @param [recommendedTracksPool] candidate tracks in the pool
     * @param [usersTracksPool] user's top tracks
     */
    private suspend fun getMostSimilarTracksBySettings(
        recommendedTracksPool: MutableList<TrackAudioFeatures>,
        usersTracksPool: MutableList<TrackAudioFeatures>
    ) {
        val fullTracks = recommendedTracksPool + usersTracksPool
        val minMax = Helper.getMinMaxFeatures(fullTracks as ArrayList<TrackAudioFeatures>)
        val recommendedToUserMap: MutableMap<TrackAudioFeatures, MutableMap<TrackAudioFeatures, Double>> =
            HashMap()
        for (recTrack in recommendedTracksPool) {
            recommendedToUserMap[recTrack] = HashMap()
            for (userTrack in usersTracksPool) {
                recommendedToUserMap[recTrack]!![userTrack] =
                    Helper.compareTrackFeatures(recTrack, userTrack, minMax, featuresList.value!!)
            }
        }
        val sorted = recommendedToUserMap.map {
            Pair(
                first = it.key,
                second = it.value.values.sorted().take(tracksCount.value!!).sum()
            )
        }.sortedBy { it.second }
        var bestOverall = sorted.take(Config.recommendedCount).map { it.first }
        bestOverall = getArtistGenres(bestOverall)!!
        insertAll(bestOverall.map { TrackCustomEntity(it.track.trackId, it.track, it.features) })
    }

    /**
     * Prepares nodes, edges, colors for graph
     */
    private fun prepareD3RelationsDistance() {
        val allGraphData = Helper.prepareD3RelationsDistance(recommendedTracksLocal.map { TrackAudioFeatures(it.track, it.features)}.toMutableList(), Config.forceDistanceFactor)
        nodes.value = allGraphData.first
        linksDistance.value = allGraphData.second
        allGraphNodes.value = allGraphData.third.toMutableList()
    }

    /**
     * Prepares data for graph detail window
     * @param [track] selected track
     * @return list of genres with colors
     */
    fun showDetailInfo(track: Track){
        selectedTrack.value = track
        artistName.value = track.artists[0].artistName
        imageUrl.value = if (track.album.albumImages.isNotEmpty()) track.album.albumImages[0].url else ""
        isTrack.value = selectedTrack.value != null
        detailViewVisible.value = true
        detailVisibleType.value = DetailVisibleType.SINGLE
    }

    /**
     * Prepares data for graph bundle detail window
     * @param [nodeIndices] indices of the nodes contained in the bundle; indices are separated by ','.
     * @param [currentIndex] index of the selected node in the list of all nodes.
     * @return items to display in the bundle list
     */
    fun showBundleDetailInfo(nodeIndices: String, currentIndex: String): MutableList<BundleTrackFeatureItem> {
        val bundleItems = mutableListOf<BundleTrackFeatureItem>()
        val allItems = nodeIndices.split(',')
            .map { item -> allGraphNodes.value!![item.toInt()]}
            .toMutableList()
        bundleItems.add(allGraphNodes.value!![currentIndex.toInt()])
        bundleItems.addAll(allItems.toMutableList())
        detailViewVisible.value = true
        detailVisibleType.value = DetailVisibleType.BUNDLE
        return bundleItems
    }

    override fun onExpandClick(expanded: Boolean) {
        (context as MainActivity).viewModel.expanded.value = !expanded
    }

    override fun onTabExpandClick(expanded: Boolean) {
        (context as MainActivity).viewModel.tabVisible.value = !expanded
    }

    override fun onListClick() { visualState.value = VisualState.TABLE }

    override fun onGraphClick() { visualState.value = VisualState.GRAPH }

    override fun onSettingsClick() { visualState.value = VisualState.SETTINGS }

    private fun prepareD3RelationsDistanceMetrics(distFactor : Int) {
        val allGraphData = Helper.prepareD3RelationsDistance(recommendedTracksLocal.map { TrackAudioFeatures(it.track, it.features)}.toMutableList(), distFactor)
        nodes.value = allGraphData.first
        linksDistance.value = allGraphData.second
        allGraphNodes.value = allGraphData.third.toMutableList()
    }
}