package com.example.spotifyexplained.model

import com.google.gson.annotations.SerializedName

data class TrackObj(
    @SerializedName("track")
    val track: Track)

