package com.example.spotifyexplained

import com.example.spotifyexplained.model.*
import com.example.spotifyexplained.services.ApiService
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

class ApiServiceUnitTest {
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
                        "  \"items\": [\n" +
                        "   {\"track\": {}}" +
                        "  ]\n" +
                        "}").setResponseCode(200)
            )
            val response = apiService.getUserSavedTracks(limit = 20)
            val request = mockWebServer.takeRequest()
            assertThat(request.path).isEqualTo("/me/tracks?limit=20")
            assertThat(response?.body()?.items?.size).isEqualTo(1)
            assertThat(response?.body()).isInstanceOf(SavedItemsTrack::class.java)
            assertThat(response?.body()?.items).isInstanceOf(Array<TrackObj>::class.java)
            assertThat((response?.body()?.items?.map { it.track })?.get(0)).isInstanceOf(Track::class.java)
        }
    }

    @Test
    fun validateTopTracks_return_success() {
        runBlocking {
            val mockResponse = MockResponse()
            mockWebServer.enqueue(
                mockResponse.setBody("{\n" +
                        "  \"items\": [\n" +
                        "   {\"track\": {}}" +
                        "  ]\n" +
                        "}").setResponseCode(200)
            )
            val response = apiService.getUserTopTracksResp(limit = 20)
            val request = mockWebServer.takeRequest()
            assertThat(request.path).isEqualTo("/me/top/tracks?limit=20")
            assertThat(response?.body()?.items?.size).isEqualTo(1)
            assertThat(response?.body()).isInstanceOf(TopItemsTrack::class.java)
            assertThat(response?.body()?.items).isInstanceOf(Array<Track>::class.java)
            assertThat((response?.body()?.items?.map { it })?.get(0)).isInstanceOf(Track::class.java)
        }
    }

    @Test
    fun validateTopArtists_return_success() {
        runBlocking {
            val mockResponse = MockResponse()
            mockWebServer.enqueue(
                mockResponse.setBody("{\n" +
                        "  \"items\": [\n" +
                        "   {\"track\": {}}" +
                        "  ]\n" +
                        "}").setResponseCode(200)
            )
            val response = apiService.getUserTopArtists(limit = 20)
            val request = mockWebServer.takeRequest()
            assertThat(request.path).isEqualTo("/me/top/artists?limit=20")
            assertThat(response?.body()?.items?.size).isEqualTo(1)
            assertThat(response?.body()).isInstanceOf(TopItemsArtist::class.java)
            assertThat(response?.body()?.items).isInstanceOf(Array<Artist>::class.java)
            assertThat((response?.body()?.items?.map { it })?.get(0)).isInstanceOf(Artist::class.java)
        }
    }

    @Test
    fun validateGetRecommendations_return_success() {
        runBlocking {
            val mockResponse = MockResponse()
            mockWebServer.enqueue(
                mockResponse.setBody("{\n" +
                        "  \"tracks\": [\n" +
                        "   {\"track\": {}}" +
                        "  ]\n" +
                        "}").setResponseCode(200)
            )
            val response = apiService.getRecommendationsResp(seed_artist = "", seed_tracks = "", seed_genres = "", limit = 20)
            val request = mockWebServer.takeRequest()
            assertThat(request.path).isEqualTo("/recommendations/?seed_artists=&seed_tracks=&seed_genres=&limit=20")
            assertThat(response?.body()?.tracks?.size).isEqualTo(1)
            assertThat(response?.body()).isInstanceOf(RecommendedTracks::class.java)
            assertThat(response?.body()?.tracks).isInstanceOf(Array<Track>::class.java)
            assertThat((response?.body()?.tracks?.map { it })?.get(0)).isInstanceOf(Track::class.java)
        }
    }

    @Test
    fun validateGetGenres_return_success() {
        runBlocking {
            val mockResponse = MockResponse()
            mockWebServer.enqueue(
                mockResponse.setBody("{\n" +
                        "  \"genres\": [\n" +
                        "   \"pop\"" +
                        "  ]\n" +
                        "}").setResponseCode(200)
            )
            val response = apiService.getAvailableGenres()
            val request = mockWebServer.takeRequest()
            assertThat(request.path).isEqualTo("/recommendations/available-genre-seeds")
            assertThat(response?.body()?.seeds?.size).isEqualTo(1)
            assertThat(response?.body()).isInstanceOf(GenreSeeds::class.java)
            assertThat(response?.body()?.seeds).isInstanceOf(Array<String>::class.java)
            assertThat((response?.body()?.seeds?.map { it })?.get(0)).isInstanceOf(String::class.java)
        }
    }

    @Test
    fun validateGetArtists_return_success() {
        runBlocking {
            val mockResponse = MockResponse()
            mockWebServer.enqueue(
                mockResponse.setBody("{\n" +
                        "  \"artists\": [\n" +
                        "   {}" +
                        "  ]\n" +
                        "}").setResponseCode(200)
            )
            val response = apiService.getArtists("")
            val request = mockWebServer.takeRequest()
            assertThat(request.path).isEqualTo("/artists?ids=")
            assertThat(response?.body()?.artists?.size).isEqualTo(1)
            assertThat(response?.body()).isInstanceOf(ArtistsList::class.java)
            assertThat(response?.body()?.artists).isInstanceOf(Array<Artist>::class.java)
            assertThat((response?.body()?.artists?.map { it })?.get(0)).isInstanceOf(Artist::class.java)
        }
    }

    @Test
    fun validateGetArtistTopTracks_return_success() {
        runBlocking {
            val mockResponse = MockResponse()
            mockWebServer.enqueue(
                mockResponse.setBody("{\n" +
                        "  \"tracks\": [\n" +
                        "   {\"track\": {}}" +
                        "  ]\n" +
                        "}").setResponseCode(200)
            )
            val response = apiService.getArtistTopTracks(id = "aaa", market = "SK")
            val request = mockWebServer.takeRequest()
            assertThat(request.path).isEqualTo("/artists/aaa/top-tracks?market=SK")
            assertThat(response?.body()?.tracks?.size).isEqualTo(1)
            assertThat(response?.body()).isInstanceOf(TracksList::class.java)
            assertThat(response?.body()?.tracks).isInstanceOf(Array<Track>::class.java)
            assertThat((response?.body()?.tracks?.map { it })?.get(0)).isInstanceOf(Track::class.java)
        }
    }

    @Test
    fun validateGetTracks_return_success() {
        runBlocking {
            val mockResponse = MockResponse()
            mockWebServer.enqueue(
                mockResponse.setBody("{\n" +
                        "  \"tracks\": [\n" +
                        "   {\"track\": {}}" +
                        "  ]\n" +
                        "}").setResponseCode(200)
            )
            val response = apiService.getTracks(ids = "a,b,c,d")
            val request = mockWebServer.takeRequest()
            assertThat(request.path).isEqualTo("/tracks?ids=a%2Cb%2Cc%2Cd")
            assertThat(response?.body()?.tracks?.size).isEqualTo(1)
            assertThat(response?.body()).isInstanceOf(TracksList::class.java)
            assertThat(response?.body()?.tracks).isInstanceOf(Array<Track>::class.java)
            assertThat((response?.body()?.tracks?.map { it })?.get(0)).isInstanceOf(Track::class.java)
        }
    }

    @Test
    fun validateGetArtist_RelatedArtists_return_success() {
        runBlocking {
            val mockResponse = MockResponse()
            mockWebServer.enqueue(
                mockResponse.setBody("{\n" +
                        "  \"artists\": [\n" +
                        "   {\"artist\": {}}" +
                        "  ]\n" +
                        "}").setResponseCode(200)
            )
            val response = apiService.getRelatedArtists(id = "abcd")
            val request = mockWebServer.takeRequest()
            assertThat(request.path).isEqualTo("/artists/abcd/related-artists")
            assertThat(response?.body()?.artists?.size).isEqualTo(1)
            assertThat(response?.body()).isInstanceOf(ArtistsList::class.java)
            assertThat(response?.body()?.artists).isInstanceOf(Array<Artist>::class.java)
            assertThat((response?.body()?.artists?.map { it })?.get(0)).isInstanceOf(Artist::class.java)
        }
    }

    @Test
    fun validateGetAudioFeatures_return_success() {
        runBlocking {
            val mockResponse = MockResponse()
            mockWebServer.enqueue(
                mockResponse.setBody("{\n" +
                        "  \"audio_features\": [\n" +
                        "   {\"artist\": {}}" +
                        "  ]\n" +
                        "}").setResponseCode(200)
            )
            val response = apiService.getTracksAudioFeatures(ids = "1,2,3,4,5")
            val request = mockWebServer.takeRequest()
            assertThat(request.path).isEqualTo("/audio-features?ids=1%2C2%2C3%2C4%2C5")
            assertThat(response?.body()?.audio_features?.size).isEqualTo(1)
            assertThat(response?.body()).isInstanceOf(FeaturesList::class.java)
            assertThat(response?.body()?.audio_features).isInstanceOf(Array<AudioFeatures>::class.java)
            assertThat((response?.body()?.audio_features?.map { it })?.get(0)).isInstanceOf(AudioFeatures::class.java)
        }
    }

    @Test
    fun validateGetSearch_return_success() {
        runBlocking {
            val mockResponse = MockResponse()
            mockWebServer.enqueue(
                mockResponse.setBody("{\n" +
                        "  \"tracks\": {\n" +
                        "   \"items\": [\n" +
                        "    {\"track\": {}}\n" +
                        "   ]\n" +
                        "  }\n" +
                        "}").setResponseCode(200)
            )
            val response = apiService.getSearch(query = "a", type = "Tracks", limit = 20, offset = 10)
            val request = mockWebServer.takeRequest()
            assertThat(request.path).isEqualTo("/search?q=a&type=Tracks&limit=20&offset=10")
            assertThat(response?.body()?.tracksObj?.tracks?.size).isEqualTo(1)
            assertThat(response?.body()).isInstanceOf(SearchTracksObj::class.java)
            assertThat(response?.body()?.tracksObj).isInstanceOf(SearchTracksList::class.java)
            assertThat(response?.body()?.tracksObj?.tracks).isInstanceOf(Array<Track>::class.java)
            assertThat((response?.body()?.tracksObj?.tracks?.map { it })?.get(0)).isInstanceOf(Track::class.java)
        }
    }
}