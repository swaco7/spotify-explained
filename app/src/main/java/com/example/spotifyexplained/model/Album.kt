package com.example.spotifyexplained.model

import androidx.room.Embedded
import com.google.gson.annotations.SerializedName

data class Album(
    @SerializedName("id")
    val albumId: String,

    @SerializedName("images")
    val albumImages: Array<Image>,

    @SerializedName("name")
    val albumName: String)
