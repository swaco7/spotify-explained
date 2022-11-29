package com.example.spotifyexplained.model

import com.google.gson.annotations.SerializedName

data class UserProfile(
    @SerializedName("id")
    val id: String,

    @SerializedName("uri")
    val uri: String,
)

