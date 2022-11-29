package com.example.spotifyexplained.model

import com.google.gson.annotations.SerializedName

data class TokenHeaders(
    @SerializedName("Authorization")
    var basic: String,

    @SerializedName("Content-Type")
    var contentType: String
)
