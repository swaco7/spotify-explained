package com.example.spotifyexplained.services

import com.example.spotifyexplained.model.Constants
import com.example.spotifyexplained.model.TokenRequest
import com.example.spotifyexplained.model.TokenResponse
import com.example.spotifyexplained.model.UserResponse
import retrofit2.Call
import retrofit2.http.*

interface ApiAuthService {
    @POST("api/token?grant_type=client_credentials")
    fun getToken(@Header("Content-Type") content_type: String, @Header("Authorization") token: String): Call<TokenResponse>
}