package com.example.spotifyexplained.services

import com.example.spotifyexplained.model.*
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    @GET("users/risavco6")
    fun getUser() : Call<UserResponse>

    @GET("me")
    suspend fun getProfile(): Response<UserProfile>

    @GET("me/tracks")
    suspend fun getUserSavedTracks(@Query("limit") limit: Int) : Response<SavedItemsTrack>

    @GET("me/top/tracks")
    suspend fun getUserTopTracksResp(@Query("limit") limit: Int) : Response<TopItemsTrack>

    @GET("me/top/artists")
    suspend fun getUserTopArtists(@Query("limit") limit: Int) : Response<TopItemsArtist>

    @POST("users/{user_id}/playlists")
    suspend fun postCreatePlaylist(@Path("user_id") userId: String, @Body playlist: PlaylistData) : Response<PlaylistDataReturned>

    @POST("playlists/{playlist_id}/tracks")
    suspend fun postTracksToPlaylist(@Path("playlist_id") playlistId: String, @Query("uris") uris: String) : Response<PlaylistAddResult>

    @GET("recommendations/")
    suspend fun getRecommendationsResp(
        @Query("seed_artists") seed_artist: String,
        @Query("seed_tracks") seed_tracks: String,
        @Query("seed_genres") seed_genres: String,
        @Query("limit") limit: Int
    ) : Response<RecommendedTracks>

    @GET("me/top/tracks")
    fun getUserTopTracks(@Query("limit") limit: Int) : Call<TopItemsTrack>

    @GET("me/top/artists")
    fun getUserTopArtistsCall(@Query("limit") limit: Int) : Call<TopItemsArtist>


    @GET("recommendations/available-genre-seeds")
    suspend fun getAvailableGenres() : Response<GenreSeeds>

    @GET("artists")
    suspend fun getArtists(@Query("ids") ids: String) : Response<ArtistsList>

    @GET("artists/{id}/top-tracks")
    suspend fun getArtistTopTracks(
        @Path("id") id: String,
        @Query("market") market: String
    ) : Response<TracksList>

    @GET("tracks")
    suspend fun getTracks(@Query("ids") ids: String) : Response<TracksList>

    @GET("artists/{id}/related-artists")
    suspend fun getRelatedArtists(@Path("id") id: String) : Response<ArtistsList>

    @GET("audio-features")
    suspend fun getTracksAudioFeatures(@Query("ids") ids: String) : Response<FeaturesList>

    @GET("search")
    suspend fun getSearch(
        @Query("q") query: String,
        @Query("type") type: String,
        @Query("limit") limit: Int,
        @Query("offset") offset: Int,
    ) : Response<SearchTracksObj>?



}