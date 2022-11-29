package com.example.spotifyexplained.model

import com.google.gson.annotations.SerializedName

data class Seed(
    @SerializedName("id")
    val id: String,

    @SerializedName("initialPoolSize")
    val initialPoolSize: Int,

    @SerializedName("type")
    val type: String
)
