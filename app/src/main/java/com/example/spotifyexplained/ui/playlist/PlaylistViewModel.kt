package com.example.spotifyexplained.ui.playlist

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import com.example.spotifyexplained.activity.MainActivity
import com.example.spotifyexplained.database.*
import com.example.spotifyexplained.general.Helper
import com.example.spotifyexplained.general.PlaylistClickHandler
import com.example.spotifyexplained.general.VisualTabClickHandler
import com.example.spotifyexplained.model.*
import com.example.spotifyexplained.repository.TrackRepository
import com.example.spotifyexplained.services.ApiHelper
import com.example.spotifyexplained.services.SessionManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

/**
 * ViewModel for playlist functionality
 * @param [activity] reference to MainActivity
 * @property [repository] repository for database
 */
class PlaylistViewModel(activity: Activity, private val repository: TrackRepository) : ViewModel(),
    PlaylistClickHandler, VisualTabClickHandler {
    val visualState = MutableLiveData<VisualState>().apply { value = VisualState.TABLE }
    val loadingState = MutableLiveData<LoadingState>().apply { value = LoadingState.LOADING }
    val artistName = MutableLiveData<String>().apply { value = "" }
    val trackName = MutableLiveData<String>().apply { value = "" }
    val imageUrl = MutableLiveData<String>().apply { value = "" }
    val isPremium = MutableLiveData<Boolean>().apply {}
    val isSaved = MutableLiveData<Boolean>().apply { value = false }
    val nextTrack = MutableLiveData<PlaylistNextTrackEntity>().apply {  }
    val currentTrackUri = MutableLiveData<String>().apply { value = "" }

    private val playlistTracksFlow: Flow<MutableList<PlaylistTrackEntity>> = repository.allPlaylistTracks
    private val featuresFlow: Flow<MutableList<FeaturesWeightEntity>> = repository.allPlaylistFeatures
    private val nextTrackFlow: Flow<MutableList<PlaylistNextTrackEntity>> = repository.allPlaylistNextTrack
    val playlistTracksLiveData: LiveData<MutableList<PlaylistTrackEntity>> = repository.allPlaylistTracks.asLiveData()
    private val tracksPoolFlow : Flow<MutableList<TrackInPoolEntity>> = repository.allTracksPool

    private fun deletePlaylistRepo() = viewModelScope.launch {
        repository.deletePlaylist()
    }

    private fun deletePlaylistNext() = viewModelScope.launch {
        repository.deletePlaylistNext()
    }

    private fun deleteFeatures() = viewModelScope.launch {
        repository.deleteFeatures()
    }

    private fun deletePlaylistItem(track: PlaylistTrackEntity) = viewModelScope.launch {
        repository.deleteFromPlaylist(track)
    }

    private fun insertPlaylistNext(track: PlaylistNextTrackEntity) = viewModelScope.launch {
        repository.insertNextPlaylist(track)
    }

    private fun insertToPlaylist(track: PlaylistTrackEntity) = viewModelScope.launch {
        repository.insertToPlaylist(track)
    }

    private fun insertFeatures(features: FeaturesWeightEntity) = viewModelScope.launch {
        repository.insertFeatures(features)
    }

    private fun updateFeatures(features: FeaturesWeightEntity) = viewModelScope.launch {
        repository.updateFeatures(features)
    }
    private val context: Context? by lazy {
        activity
    }

    private val localPlaylist = MutableLiveData<List<PlaylistTrackEntity>>().apply {}
    private val localFeatures = MutableLiveData<FeaturesWeightEntity>().apply {}
    private val localFullPool = MutableLiveData<MutableList<TrackAudioFeatures>>().apply {}

    init {
        if (SessionManager.tokenExpired()){
            (activity as MainActivity).authorizeUser()
        }  else {
            viewModelScope.launch {
                loadingState.value = LoadingState.LOADING
                tracksPoolFlow.collect { tracks ->
                    if (tracks.isNotEmpty()) {
                        val recommendedPool = tracks.filter { it.trackType == 0 }
                            .map { TrackAudioFeatures(it.track, it.features) }.toMutableList()
                        localFullPool.value = recommendedPool
                        Log.e("pool", "here")
                        nextTrackFlow.collect { next ->
                            delay(200)
                            if (next.isEmpty()) {
                                getNextTrack(localFullPool.value!!, localFeatures.value!!)
                            } else {
                                prepareNext(next[0])
                            }
                            loadingState.value = LoadingState.SUCCESS
                        }
                    } else {
                        loadingState.value = LoadingState.FAILURE
                    }
                }
            }
            viewModelScope.launch {
                playlistTracksFlow.collect {
                    localPlaylist.value = it
                }
            }
            viewModelScope.launch {
                featuresFlow.collect {
                    if (it.isNotEmpty()) {
                        localFeatures.value = it[0]
                    } else {
                        initializeFeatures()
                    }
                }
            }
        }
    }

    /**
     * Initializes features and pushes initialized features to database repository
     */
    private suspend fun initializeFeatures(){
        val userTracks = ApiHelper.getTracksAudioFeatures((context as MainActivity).viewModel.topTracks.value!!.map { it.track }) ?: mutableListOf()
        val averages = mutableListOf<Double>()
        for (index in 0 until userTracks[0].features.count()) {
            averages.add(userTracks.sumOf{ it.features.at(index)!! } / userTracks.size)
        }
        insertFeatures(FeaturesWeightEntity("features", AudioFeatures("averages", averages)))
    }

    /**
     * Find next track to show, saves it to repository
     */
    private fun getNextTrack(recommendedPool: MutableList<TrackAudioFeatures>, features: FeaturesWeightEntity){
        val discardedTracks = localPlaylist.value!!.map { it.track}
        val viableTracks = recommendedPool.filter { poolTrack -> discardedTracks.firstOrNull { it == poolTrack.track } == null  }
        val list = features.audioFeatures.asList().map { it.second!! }.toList()
        val minMax = Helper.getMinMaxFeatures(viableTracks)
        val similarityMap: MutableMap<TrackAudioFeatures, Double> = HashMap()
        for (track in viableTracks) {
            similarityMap[track] = Helper.compareTrackFeatures(track, list, minMax)
        }
        val sorted = similarityMap.toList().sortedBy { it.second }
        val nextTrack = sorted.first().first
        val nextTrackEntity = PlaylistNextTrackEntity(nextTrack.track.trackId, nextTrack.track, nextTrack.features)
        insertPlaylistNext(nextTrackEntity)
    }

    /**
     * Prepares layout values for current next track
     */
    private fun prepareNext(track: PlaylistNextTrackEntity){
        Log.e("nextTrack", "prepareNext")
        trackName.value = track.track.trackName
        artistName.value = track.track.artists[0].artistName
        imageUrl.value = track.track.album.albumImages[0].url
        nextTrack.value = track
    }

    fun deletePlaylist(){
        deletePlaylistRepo()
        deleteFeatures()
        deletePlaylistNext()
    }

    private fun deleteNext(){
        deletePlaylistNext()
    }

    fun deleteTrack(track: PlaylistTrackEntity){
        deletePlaylistItem(track)
    }

    /**
     * Updates features and saves current track to playlist, saves next track
     */
    private fun insertNext() {
        updateFeaturesPositiveFunc()
        insertToPlaylist(
            PlaylistTrackEntity(
                nextTrack.value!!.id,
                nextTrack.value!!.track,
                nextTrack.value!!.features,
                TrackFeedbackType.POSITIVE
            )
        )
        insertPlaylistNext(nextTrack.value!!)
    }

    /**
     * Updates features and saves current track to playlist
     */
    private fun insertNotLiked(value: TrackFeedbackType) {
        updateFeaturesNegativeFunc(if (value == TrackFeedbackType.NEUTRAL) 4 else 2)
        insertToPlaylist(
            PlaylistTrackEntity(
                nextTrack.value!!.id,
                nextTrack.value!!.track,
                nextTrack.value!!.features,
                value
            )
        )
    }
    /**
     * Calculates new features values and calls repository to update database
     */
    private fun updateFeaturesPositiveFunc(){
        val nextTrackFeatures = nextTrack.value!!.features.asList().map { it.second }
        val overallFeatures = localFeatures.value!!.audioFeatures.asList().map {it.second}
        val modifiedOverall = nextTrackFeatures.zip(overallFeatures) {xv, yv -> (xv!!.plus(yv!!)) / 2}
        updateFeatures(FeaturesWeightEntity("features", AudioFeatures("averages", modifiedOverall)))
    }

    /**
     * Calculates new features values and calls repository to update database
     */
    private fun updateFeaturesNegativeFunc(negativeFactor: Int){
        val nextTrackFeatures = nextTrack.value!!.features.asList().map { it.second }
        val overallFeatures = localFeatures.value!!.audioFeatures.asList().map {it.second}
        val modifiedOverall = nextTrackFeatures.zip(overallFeatures) {xv, yv -> (xv!!.minus(yv!!)) / negativeFactor}
        val overall = overallFeatures.zip(modifiedOverall) {xv, yv -> (xv!!.minus(yv))}.map { max(0.0, min(1.0,it)) }
        updateFeatures(FeaturesWeightEntity("features", AudioFeatures("averages", overall)))
    }

    override fun onLikeClick() {
        insertNext()
        deleteNext()
    }

    override fun onNeutralClick() {
        insertNotLiked(TrackFeedbackType.NEUTRAL)
        deleteNext()
    }

    override fun onDislikeClick() {
        insertNotLiked(TrackFeedbackType.NEGATIVE)
        deleteNext()
    }

    override fun onListClick() { visualState.value = VisualState.TABLE }

    override fun onGraphClick() { visualState.value = VisualState.GRAPH }

    override fun onSettingsClick() { return }

}