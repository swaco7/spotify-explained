package com.example.spotifyexplained.ui.related

import android.app.Activity
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spotifyexplained.activity.MainActivity
import com.example.spotifyexplained.model.*
import com.example.spotifyexplained.services.ApiRepository
import com.example.spotifyexplained.services.SessionManager
import kotlinx.coroutines.launch
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class RelatedGenresGraphViewModel(activity: Activity) : ViewModel() {
    val artists: MutableLiveData<List<Artist>> by lazy {
        MutableLiveData<List<Artist>>(ArrayList())
    }

    val artistsHelper: MutableLiveData<List<Artist>> by lazy {
        MutableLiveData<List<Artist>>(ArrayList())
    }

    val artistsNodes: MutableLiveData<ArrayList<D3Node>> by lazy {
        MutableLiveData<ArrayList<D3Node>>(ArrayList())
    }

    val artistsLinks: MutableLiveData<ArrayList<D3Link>> by lazy {
        MutableLiveData<ArrayList<D3Link>>(ArrayList())
    }

    init {
        if (SessionManager.tokenExpired()){
            (activity as MainActivity).authorizeUser()
        }  else {
            viewModelScope.launch {
                getUserTopArtists()
                prepareD3Relations()
            }
        }
    }

    private suspend fun getUserTopArtists() {
        val result = ApiRepository.getCustomApiService().getUserTopArtists(limit = 50)
        if (result.isSuccessful) {
            val response = result.body()
            artistsHelper.value = response!!.items.toList()
        } else {
            Log.e("error", result.errorBody().toString())
        }
    }

    private fun prepareD3Relations() {
        val relationDataNodes = ArrayList<D3Node>()
        val relationDataLinks = ArrayList<D3Link>()
        val map = HashMap<String, Int>()
        for (artist in artistsHelper.value!!) {
            for (genre in artist.genres!!) {
                map[artist.artistName] = if (map.containsKey(artist.artistName)) map.getValue(artist.artistName) + 1 else 1
                map[genre] = if (map.containsKey(genre)) map.getValue(genre) + 1 else 1
            }
        }
        for (artist in map) {
            relationDataNodes.add(D3Node(artist.key, 1, Constants.radius, Constants.defaultColor))
        }
        for (artist in artistsHelper.value!!) {
            for (genre in artist.genres!!) {
                relationDataLinks.add(D3Link(artist.artistName, genre, 1, Constants.defaultColor, LinkType.RELATED))
            }
        }
        artistsNodes.value = relationDataNodes
        artistsLinks.value = relationDataLinks
    }
}