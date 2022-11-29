package com.example.spotifyexplained.model

import androidx.room.Embedded
import com.google.gson.annotations.SerializedName

data class PlaylistDataReturned(
    @SerializedName("id")
    val id: String,
    @SerializedName("uri")
    val uri: Boolean
)
