package com.example.spotifyexplained.model

import com.google.gson.annotations.SerializedName

data class TokenRequest(
    @SerializedName("grant_type")
    var grant_type: String
)
