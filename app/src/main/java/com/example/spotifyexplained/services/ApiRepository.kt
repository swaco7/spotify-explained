package com.example.spotifyexplained.services

import android.util.Log
import com.example.spotifyexplained.general.Config
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

object ApiRepository {
    lateinit var apiService: ApiService

    fun getCustomApiService(): ApiService {
        if (!::apiService.isInitialized) {
            val httpClient = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
            httpClient.addInterceptor { chain ->
                val request: Request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer " + SessionManager.fetchToken())
                    .build()
                Log.e("request", request.toString())
                if (!SessionManager.isInternetAvailable()) {
                    throw SocketTimeoutException()
                }
                if (SessionManager.tokenExpired()){
                    throw com.example.spotifyexplained.services.UserNotAuthorizedException("error")
                }
                chain.proceed(request)
            }
            val gson = GsonBuilder().create()
            val retrofit = Retrofit.Builder()
                .baseUrl(Config.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(httpClient.build())
                .build()

            apiService = retrofit.create(ApiService::class.java)
        }
        return apiService
    }
}

class UserNotAuthorizedException(message: String): SocketTimeoutException(message)