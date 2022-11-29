package com.example.spotifyexplained.activity

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.*
import com.example.spotifyexplained.database.ArtistRecommendEntity
import com.example.spotifyexplained.database.UserArtistEntity
import com.example.spotifyexplained.general.Helper
import com.example.spotifyexplained.model.*
import com.example.spotifyexplained.repository.TrackRepository
import com.example.spotifyexplained.services.ApiHelper
import com.example.spotifyexplained.services.SessionManager
import com.spotify.android.appremote.api.SpotifyAppRemote
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.max
import kotlin.math.min
import kotlin.math.truncate


class MainViewModel : ViewModel() {
    var poolIsLoading = MutableLiveData<Boolean>().apply{
        value = false
    }
    val loadingProgress = MutableLiveData<Int>().apply {
        Log.e("loadingProgress", "-- init")
        value = 0
    }
    val phase = MutableLiveData<Int>().apply {
        value = 0
    }
    val progressText = MutableLiveData<String>().apply {
        value = ""
    }
    val expanded = MutableLiveData<Boolean>().apply {
        value = false
    }
    val tabVisible = MutableLiveData<Boolean>().apply {
        value = true
    }
    lateinit var job : Job

    val state = MutableLiveData<VisualState>().apply {
        value = VisualState.TABLE
    }
    val loadingState = MutableLiveData<LoadingState>().apply {
        value = LoadingState.LOADING
    }

    val topArtists: MutableLiveData<MutableList<Artist>> by lazy {
        MutableLiveData<MutableList<Artist>>(mutableListOf())
    }

    val topTracks: MutableLiveData<MutableList<TrackAudioFeatures>> by lazy {
        MutableLiveData<MutableList<TrackAudioFeatures>>(mutableListOf())
    }

    override fun onCleared() {
        super.onCleared()
        Log.e("mainCleared", "--here")
    }
}