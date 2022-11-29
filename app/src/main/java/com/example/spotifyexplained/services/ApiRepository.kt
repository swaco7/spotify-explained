package com.example.spotifyexplained.services

import android.content.Context
import android.util.Log
import com.github.slashrootv200.retrofithtmlconverter.HtmlConverterFactory
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

object ApiRepository {
    lateinit var apiService: ApiService

    fun getCustomApiService(): ApiService {
        if (!::apiService.isInitialized) {
            val httpClient = OkHttpClient.Builder()
            httpClient.addInterceptor { chain ->
                if (!SessionManager.isInternetAvailable()) {
                    throw NoConnectivityException()
                }
                val request: Request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer " + SessionManager.fetchToken())
                    .build()
                Log.e("request", request.toString())

                chain.proceed(request)
            }
            val gson = GsonBuilder().create()
            val retrofit = Retrofit.Builder()
                .baseUrl("https://api.spotify.com/v1/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(httpClient.build())
                .build()

            apiService = retrofit.create(ApiService::class.java)
        }

        return apiService
    }
}

public class NoConnectivityException : IOException() {
    override val message: String = "No connection message"
}