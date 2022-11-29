package com.example.spotifyexplained.model

import com.google.gson.annotations.SerializedName

data class TopItemsArtist(
    @SerializedName("items")
    val items: Array<Artist>
)
