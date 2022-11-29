package com.example.spotifyexplained.model

import com.google.gson.annotations.SerializedName

data class SavedItemsTrack(
    @SerializedName("items")
    val items: Array<TrackObj>
    )