package com.example.spotifyexplained.activity

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.spotifyexplained.model.Artist
import com.example.spotifyexplained.model.enums.LoadingState
import com.example.spotifyexplained.model.TrackAudioFeatures
import com.example.spotifyexplained.model.enums.VisualState
import kotlinx.coroutines.Job


class MainViewModel : ViewModel() {
    val poolIsLoading = MutableLiveData<Boolean>().apply{
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

    val state = MutableLiveData<VisualState>().apply {
        value = VisualState.TABLE
    }
    val loadingState = MutableLiveData<LoadingState>().apply {
        value = LoadingState.LOADING
    }

    val connectionError = MutableLiveData<Boolean>().apply {
        value = false
    }

    val authenticationPending = MutableLiveData<Boolean>().apply{
        value = false
    }

    val topArtists: MutableLiveData<List<Artist>> by lazy {
        MutableLiveData<List<Artist>>(listOf())
    }

    val topTracks: MutableLiveData<List<TrackAudioFeatures>> by lazy {
        MutableLiveData<List<TrackAudioFeatures>>(listOf())
    }

    lateinit var job : Job

    fun showError(){
        if (!connectionError.value!!){
            connectionError.value = true
        }
    }
}