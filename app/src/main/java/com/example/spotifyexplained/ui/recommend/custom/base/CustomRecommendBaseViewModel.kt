package com.example.spotifyexplained.ui.recommend.custom.base

import android.app.Activity
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.example.spotifyexplained.R
import com.example.spotifyexplained.activity.MainActivity
import com.example.spotifyexplained.database.TrackInPoolEntity
import com.example.spotifyexplained.general.ExpandClickHandler
import com.example.spotifyexplained.model.*
import com.example.spotifyexplained.repository.TrackRepository
import com.example.spotifyexplained.services.ApiHelper
import com.example.spotifyexplained.services.SessionManager
import com.example.spotifyexplained.ui.general.HelpDialogFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * ViewModel for custom recommend base, handles loading
 * @param [activity] reference to MainActivity
 * @param [repository] repository for database
 */
class CustomRecommendBaseViewModel(activity: Activity, private val repository: TrackRepository) : ViewModel(), ExpandClickHandler {
    val loadingState = MutableLiveData<LoadingState>().apply { value = LoadingState.LOADING }
    val loadingProgress = MutableLiveData<Int>().apply { value = 0 }
    val progressText = MutableLiveData<String>().apply { value = "" }
    val phase = MutableLiveData<Int>().apply { value = 0 }
    val tabVisible = MutableLiveData<Boolean>().apply { value = false }
    val expanded = MutableLiveData<Boolean>().apply { value = false }

    val tracks: MutableLiveData<List<Track>> by lazy {
        MutableLiveData<List<Track>>(ArrayList())
    }

    private val topTracks: MutableLiveData<List<Track>> by lazy {
        MutableLiveData<List<Track>>(ArrayList())
    }

    private val savedTracks: MutableLiveData<List<Track>> by lazy {
        MutableLiveData<List<Track>>(ArrayList())
    }

    private val tracksPoolFromDatabaseFlow : Flow<MutableList<TrackInPoolEntity>> = repository.allTracksPool

    private suspend fun insertListToPool(tracks: List<TrackInPoolEntity>) {
        repository.insertListToPool(tracks)
    }

    private val mainActivity: MainActivity by lazy {
        activity as MainActivity
    }

    init {
        if (SessionManager.tokenExpired()) {
            (activity as MainActivity).authorizeUser()
        } else {
            if (!mainActivity.viewModel.poolIsLoading.value!!) {
                viewModelScope.launch {
                    getUserTracks()
                    tracksPoolFromDatabaseFlow.collect { pool ->
                        if (pool.isEmpty() && !mainActivity.viewModel.poolIsLoading.value!!) {
                            reload(pool.isEmpty())
                        }
                    }
                }
            }
        }
    }

    private suspend fun getUserTracks() {
        topTracks.value = ApiHelper.getUserTopTracks(50)
        savedTracks.value = ApiHelper.getUserSavedTracks(Constants.savedTracksLimit)
    }

    /**
     * Pushes pool to repository, clears tables for currently stored custom recommendations
     * @param [pool] tracks to push
     */
    private suspend fun savePool(pool : Pair<Int, List<TrackAudioFeatures>>) {
        clearData()
        insertListToPool(pool.second.map { TrackInPoolEntity(it.track.trackId, it.track, it.features, if (pool.second.indexOf(it) < pool.first) 0 else 1)})
        mainActivity.viewModel.poolIsLoading.value = false
    }

    /**
     * Prepares full pool of tracks if it currently does not exist
     */
    fun reload(shouldLoadPool : Boolean) {
        if (SessionManager.tokenExpired()) {
            mainActivity.authorizeUser()
        } else {
            mainActivity.viewModel.job = mainActivity.lifecycleScope.launch {
                mainActivity.viewModel.poolIsLoading.value = true
                getUserTracks()
                if (shouldLoadPool) {
                    savePool(getTracksPool())
                } else {
                    tracksPoolFromDatabaseFlow.collect { tracks ->
                        Log.e("fullPool", tracks.size.toString())
                    }
                }
            }
        }
    }

    fun clearData() {
        viewModelScope.launch {
            repository.deletePool()
        }
    }

    fun deleteCaches() {
        viewModelScope.launch {
            repository.delete()
            repository.deleteSpecific()
            repository.deleteCustom()
        }
    }

    fun infoClicked(){
        val dialog = HelpDialogFragment(mainActivity.resources.getString(R.string.custom_recommend_info_text))
        val fragmentManager = mainActivity.supportFragmentManager
        dialog.show(fragmentManager, "help")
    }

    /**
     * Gathers tracks for pool
     * @return pair consisting of the number representing size of pool
     * and list consisting of track pool and user's top tracks
     */
    private suspend fun getTracksPool() : Pair<Int, List<TrackAudioFeatures>> {
        mainActivity.viewModel.phase.value = 1
        phase.value = 1
        val userTracks = (savedTracks.value!! + topTracks.value!!).distinctBy { it.trackId }.take(5)
        val userTracksFeatures = ApiHelper.getTracksAudioFeatures(userTracks.toMutableList()) ?: mutableListOf()
        val relatedArtists = mutableListOf<Artist>()

        // Gather related artists from user's tracks
        for (index in userTracks.indices.take(Constants.trackSizeForCustomRecommend)) {
            val trackArtist =
                mainActivity.viewModel.topArtists.value!!.firstOrNull { it.artistId == userTracks[index].artists[0].artistId }
            relatedArtists.addAll(
                trackArtist?.related_artists ?: ApiHelper.getRelatedArtists(
                    userTracks[index].artists[0].artistId
                ) ?: mutableListOf()
            )
            if (index % 10 == 0 && index > 0) {
                mainActivity.viewModel.loadingProgress.value = ((index.toDouble() / Constants.trackSizeForCustomRecommend) * 100).toInt()
                Log.e("related", "$index / ${userTracks.size}")
                delay(400)
            }
        }

        // Gather top tracks for all artists
        val distinctRelatedArtist = relatedArtists.distinctBy { it.artistId }
        mainActivity.viewModel.phase.value = 2
        phase.value = 2
        val artistsTopTracks = mutableListOf<Track>()
        val distinctRelatedParts = distinctRelatedArtist.chunked(10).toMutableList()
        for (part in distinctRelatedParts) {
            for (artist in part) {
                artistsTopTracks.addAll(
                    ApiHelper.getArtistTopTracks(artist.artistId) ?: mutableListOf()
                )
            }
            mainActivity.viewModel.loadingProgress.value = (((distinctRelatedParts.indexOf(part).toDouble())/ distinctRelatedParts.size) * 100).toInt()
            Log.e("topTracks", "${distinctRelatedParts.indexOf(part)} / ${distinctRelatedParts.size} - ${loadingProgress.value} ")
            delay(500)
        }

        // Get audio features for all tracks
        mainActivity.viewModel.phase.value = 3
        phase.value = 3
        val customRecommendedTracksFeatures = mutableListOf<TrackAudioFeatures>()
        val chunkedArtistTopTracks = artistsTopTracks.chunked(100).toMutableList()
        for (chunk in chunkedArtistTopTracks) {
            Log.e("request", chunkedArtistTopTracks.indexOf(chunk).toString())
            customRecommendedTracksFeatures.addAll(
                ApiHelper.getTracksAudioFeaturesContext(
                    ApiHelper.getTracksSeedsString(
                        chunk.toMutableList()
                    ), artistsTopTracks, mainActivity!!
                )
                    ?: mutableListOf()
            )
            mainActivity.viewModel.loadingProgress.value = ((chunkedArtistTopTracks.indexOf(chunk).toDouble() / chunkedArtistTopTracks.size) * 100).toInt()
            if (chunkedArtistTopTracks.indexOf(chunk) % 10 == 0 && chunkedArtistTopTracks.indexOf(chunk) > 0) {
                delay(250)
            }
        }
        mainActivity.viewModel.loadingProgress.value = 100
        mainActivity.viewModel.phase.value = 4
        phase.value = 4
        Log.e("allTracks", customRecommendedTracksFeatures.size.toString())
        val fullTracks = customRecommendedTracksFeatures + userTracksFeatures
        return Pair(customRecommendedTracksFeatures.size, fullTracks)
    }

    override fun onExpandClick(expanded: Boolean) { mainActivity.viewModel.expanded.value = !expanded }

    override fun onTabExpandClick(expanded: Boolean) { mainActivity.viewModel.tabVisible.value = !expanded }
}