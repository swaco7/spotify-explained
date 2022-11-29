package com.example.spotifyexplained.model

import com.google.gson.annotations.SerializedName

data class SearchTracksObj(
    @SerializedName("tracks")
    val tracksObj: SearchTracksList
    )
