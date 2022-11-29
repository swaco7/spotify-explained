package com.example.spotifyexplained

import com.example.spotifyexplained.model.LoadingState
import com.example.spotifyexplained.repository.TrackRepository
import com.example.spotifyexplained.services.ApiService
import com.example.spotifyexplained.ui.home.HomeViewModel
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.setMain
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import org.mockito.Mock
import org.mockito.Mockito
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class LoginAPIServiceTest {

    @Mock
    lateinit var mockWebServer: MockWebServer
    @Mock
    lateinit var apiService: ApiService
    lateinit var gson: Gson

    @Before
    fun setup() {
        gson = GsonBuilder().create()
        mockWebServer = MockWebServer()
        apiService = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(ApiService::class.java)
    }

    @After
    fun deconstruct() {
        mockWebServer.shutdown()
    }

    @Test
    fun validateUserData_return_success() {
        runBlocking {
            val mockResponse = MockResponse()
            mockWebServer.enqueue(
                mockResponse.setBody("{\n" +
                        "  \"href\": \"https://api.spotify.com/v1/me/shows?offset=0&limit=20\\n\",\n" +
                        "  \"items\": [\n" +
                        "    {}\n" +
                        "  ],\n" +
                        "  \"limit\": 20,\n" +
                        "  \"next\": \"https://api.spotify.com/v1/me/shows?offset=1&limit=1\",\n" +
                        "  \"offset\": 0,\n" +
                        "  \"previous\": \"https://api.spotify.com/v1/me/shows?offset=1&limit=1\",\n" +
                        "  \"total\": 4\n" +
                        "}").setResponseCode(200)
            )
            val response = apiService.getUserSavedTracks(limit = 20)
            val request = mockWebServer.takeRequest()
            assertThat(request.path).isEqualTo("/me/tracks?limit=20")
            assertThat(response.body()?.items?.size).isEqualTo(5)
        }
    }

}