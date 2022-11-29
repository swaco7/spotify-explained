package com.example.spotifyexplained.model

import com.google.gson.annotations.SerializedName

data class TopItemsTrack(
    @SerializedName("items")
    val items: Array<Track>
    )