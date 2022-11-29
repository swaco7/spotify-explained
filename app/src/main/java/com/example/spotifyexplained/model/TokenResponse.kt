package com.example.spotifyexplained.model

import com.google.gson.annotations.SerializedName

data class TokenResponse (
    @SerializedName("access_token")
    var token: String,

    @SerializedName("status_code")
    var statusCode: Int
)