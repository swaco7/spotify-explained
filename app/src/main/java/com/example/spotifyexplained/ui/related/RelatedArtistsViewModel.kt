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

class RelatedArtistsViewModel(activity: Activity) : ViewModel() {
    val artists: MutableLiveData<List<Artist>> by lazy {
        MutableLiveData<List<Artist>>(ArrayList())
    }

    val artistsHelper: MutableLiveData<List<Artist>> by lazy {
        MutableLiveData<List<Artist>>(ArrayList())
    }


    val relatedArtists: MutableLiveData<List<Artist>> by lazy {
        MutableLiveData<List<Artist>>(ArrayList())
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
}