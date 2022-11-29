package com.example.spotifyexplained.model

import com.google.gson.annotations.SerializedName

data class SearchTracksList(
    @SerializedName("items")
    val tracks: Array<Track>
    )
