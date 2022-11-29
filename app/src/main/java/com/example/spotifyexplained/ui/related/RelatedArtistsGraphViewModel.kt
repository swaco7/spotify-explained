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

class RelatedArtistsGraphViewModel(activity: Activity) : ViewModel() {
    val artists: MutableLiveData<List<Artist>> by lazy {
        MutableLiveData<List<Artist>>(ArrayList())
    }

    val artistsHelper: MutableLiveData<List<Artist>> by lazy {
        MutableLiveData<List<Artist>>(ArrayList())
    }


    val relatedArtists: MutableLiveData<List<Artist>> by lazy {
        MutableLiveData<List<Artist>>(ArrayList())
    }

    val artistsRelations : MutableLiveData<ArrayList<Array<String>>> by lazy {
        MutableLiveData<ArrayList<Array<String>>>(ArrayList())
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
                for (artist in artistsHelper.value!!){
                    getRelatedArtists(artist.artistId)
                }
                prepareD3Relations()
            }
        }
    }

    private suspend fun getUserTopArtists() {
        val result = ApiRepository.getCustomApiService().getUserTopArtists(limit = 30)
        if (result.isSuccessful) {
            val response = result.body()
            artistsHelper.value = response!!.items.toList()
            Log.e("result", artistsHelper.value.toString())
        } else {
            Log.e("error", result.errorBody().toString())
        }
    }

    private suspend fun getRelatedArtists(artistId : String) {
        val result = ApiRepository.getCustomApiService().getRelatedArtists(id = artistId)
        if (result.isSuccessful) {
            val response = result.body()
            relatedArtists.value = response!!.artists.toList()
            artistsHelper.value!!.first { it.artistId == artistId }.related_artists = relatedArtists.value!!
            artists.value = artistsHelper.value!!
            Log.e("result", artists.value.toString())
        } else {
            Log.e("error", result.errorBody().toString())
        }
    }

    private fun getArtistsSeedsString() : String {
        val builder = StringBuilder()
        for (artist in artists.value!!) {
            builder.append(artist.artistId).append(",")
        }
        return builder.toString()
    }

    private fun prepareArtistsRelations() {
        val relationData = ArrayList<Array<String>>()
        val map = HashMap<String, Int>()
        for (artist in artists.value!!) {
            for (relatedArtist in artist.related_artists) {
                map[artist.artistName] = if (map.containsKey(artist.artistName)) map.getValue(artist.artistName) + 1 else 1
                map[relatedArtist.artistName] = if (map.containsKey(relatedArtist.artistName)) map.getValue(relatedArtist.artistName) + 1 else 1
            }
        }
        for (artist in artists.value!!) {
            for (relatedArtist in artist.related_artists) {
                if (map.getValue(artist.artistName) > 2 && map.getValue(relatedArtist.artistName) > 2)
                    relationData.add(arrayOf(artist.artistName, relatedArtist.artistName))
            }
        }
        artistsRelations.value = relationData
    }

    private fun prepareD3Relations() {
        val relationDataNodes = ArrayList<D3Node>()
        val relationDataLinks = ArrayList<D3Link>()
        val map = HashMap<String, Int>()
        for (artist in artists.value!!) {
            for (relatedArtist in artist.related_artists) {
                map[artist.artistName] = if (map.containsKey(artist.artistName)) map.getValue(artist.artistName) + 1 else 1
                map[relatedArtist.artistName] = if (map.containsKey(relatedArtist.artistName)) map.getValue(relatedArtist.artistName) + 1 else 1
            }
        }
        for (artist in map) {
            relationDataNodes.add(D3Node(artist.key, 1, Constants.radius, Constants.defaultColor))
        }
        for (artist in artists.value!!) {
            for (relatedArtist in artist.related_artists) {
                relationDataLinks.add(D3Link(artist.artistName, relatedArtist.artistName, 1, Constants.defaultColor, LinkType.RELATED))
            }
        }
        artistsNodes.value = relationDataNodes
        artistsLinks.value = relationDataLinks
    }
}