package com.example.spotifyexplained.services

import android.content.Context
import android.util.Log
import com.example.spotifyexplained.activity.MainActivity
import com.example.spotifyexplained.general.Config
import com.example.spotifyexplained.model.*
import com.example.spotifyexplained.model.enums.RecommendSeedType
import retrofit2.Response
import java.net.SocketTimeoutException

object ApiHelper {
    suspend fun getUserTopTracks(limit: Int, context: Context): MutableList<Track>? {
        var result : Response<TopItemsTrack>? = null
        try {
            result = ApiRepository.getCustomApiService().getUserTopTracksResp(limit = limit)
        } catch (exception: SocketTimeoutException) {
            when (exception) {
                is UserNotAuthorizedException -> (context as MainActivity).authorizeUser()
                else -> (context as MainActivity).viewModel.showError()
            }
        }
        return if (result?.isSuccessful == true) {
            result.body()?.items?.toMutableList()
        } else {
            null
        }
    }

    suspend fun addTracksToPlaylist(playlistId: String, tracks: MutableList<Track>, context: Context): PlaylistAddResult? {
        var result : Response<PlaylistAddResult>? = null
        try {
            result = ApiRepository.getCustomApiService().postTracksToPlaylist(playlistId, getTracksUris(tracks))
        } catch (exception: SocketTimeoutException) {
            when (exception) {
                is UserNotAuthorizedException -> (context as MainActivity).authorizeUser()
                else -> (context as MainActivity).viewModel.showError()
            }
        }
        return if (result?.isSuccessful == true) {
            result.body()
        } else {
            null
        }
    }

    suspend fun createPlaylist(name: String, description: String, isPublic: Boolean, isCollaborative: Boolean, context: Context): PlaylistDataReturned? {
        var result : Response<PlaylistDataReturned>? = null
        try {
            result = ApiRepository.getCustomApiService().postCreatePlaylist(
                SessionManager.getUserId()!!,
                PlaylistData(name, isPublic, isCollaborative, description)
            )
        } catch (exception: SocketTimeoutException) {
            when (exception) {
                is UserNotAuthorizedException -> (context as MainActivity).authorizeUser()
                else -> (context as MainActivity).viewModel.showError()
            }
        }
        return if (result?.isSuccessful == true) {
            result.body()
        } else {
            null
        }
    }

    suspend fun getProfile(context: Context): UserProfile? {
        var result : Response<UserProfile>? = null
        try {
            result = ApiRepository.getCustomApiService().getProfile()
        } catch (exception: SocketTimeoutException) {
            when (exception) {
                is UserNotAuthorizedException -> (context as MainActivity).authorizeUser()
                else -> (context as MainActivity).viewModel.showError()
            }
        }
        return if (result?.isSuccessful == true) {
            result.body()
        } else {
            Log.e("error", result?.message() ?: "error")
            null
        }
    }

    suspend fun getUserSavedTracks(limit : Int, context: Context): MutableList<Track>?{
        var result : Response<SavedItemsTrack>? = null
        try {
            result = ApiRepository.getCustomApiService().getUserSavedTracks(limit = limit)
        } catch (exception: SocketTimeoutException) {
            when (exception) {
                is UserNotAuthorizedException -> (context as MainActivity).authorizeUser()
                else -> (context as MainActivity).viewModel.showError()
            }
        }
        return if (result?.isSuccessful == true) {
            result.body()?.items?.map { it.track }?.toMutableList()
        } else {
            null
        }
    }

    suspend fun getUserTopArtists(limit : Int, context: Context) : MutableList<Artist>?{
        var result : Response<TopItemsArtist>? = null
        try {
            result = ApiRepository.getCustomApiService().getUserTopArtists(limit = limit)
        } catch (exception: SocketTimeoutException) {
            when (exception) {
                is UserNotAuthorizedException -> (context as MainActivity).authorizeUser()
                else -> (context as MainActivity).viewModel.showError()
            }
        }
        return if (result?.isSuccessful == true) {
            result.body()?.items?.toMutableList()
        } else {
            null
        }
    }

    suspend fun getRelatedArtists(artistId : String, context: Context) : MutableList<Artist>?{
        var result : Response<ArtistsList>? = null
        try {
            result = ApiRepository.getCustomApiService().getRelatedArtists(id = artistId)
        } catch (exception: SocketTimeoutException) {
            when (exception) {
                is UserNotAuthorizedException -> (context as MainActivity).authorizeUser()
                else -> (context as MainActivity).viewModel.showError()
            }
        }
        return if (result?.isSuccessful == true) {
            result.body()?.artists?.toMutableList()
        } else {
            null
        }
    }

    suspend fun getArtistTopTracks(artistId : String, context: Context) : MutableList<Track>?{
        var result : Response<TracksList>? = null
        try {
            result = ApiRepository.getCustomApiService().getArtistTopTracks(id = artistId, market = "SK")
        } catch (exception: SocketTimeoutException) {
            when (exception) {
                is UserNotAuthorizedException -> (context as MainActivity).authorizeUser()
                else -> (context as MainActivity).viewModel.showError()
            }
        }
        return if (result?.isSuccessful == true) {
            result.body()?.tracks?.toMutableList()
        } else {
            Log.e("error", result?.message() ?: "")
            null
        }
    }

    suspend fun getTracksAudioFeatures(tracks: List<Track>, context: Context): List<TrackAudioFeatures>? {
        var result : Response<FeaturesList>? = null
        try {
            result = ApiRepository.getCustomApiService().getTracksAudioFeatures(ids = getTracksSeedsString(tracks))
        } catch (exception: SocketTimeoutException) {
            when (exception) {
                is UserNotAuthorizedException -> (context as MainActivity).authorizeUser()
                else -> (context as MainActivity).viewModel.showError()
            }
        }
        return if (result?.isSuccessful == true) {
            val response = result.body()
            val list = ArrayList<TrackAudioFeatures>()
            for (trackFeatures in response?.audio_features ?: arrayOf()) {
                if (tracks.firstOrNull { it.trackId == trackFeatures.featuresId } != null) {
                    list.add(
                        TrackAudioFeatures(
                            tracks.firstOrNull { it.trackId == trackFeatures.featuresId }!!,
                            trackFeatures
                        )
                    )
                }
            }
            list
        } else {
            Log.e("error", result?.message() ?: "")
            null
        }
    }

    suspend fun getTracks(trackId: String, context: Context) : Track? {
        var result : Response<TracksList>? = null
        try {
            result = ApiRepository.getCustomApiService().getTracks(ids = trackId)
        } catch (exception: SocketTimeoutException) {
            when (exception) {
                is UserNotAuthorizedException -> (context as MainActivity).authorizeUser()
                else -> (context as MainActivity).viewModel.showError()
            }
        }
        return if (result?.isSuccessful == true) {
            result.body()?.tracks?.get(0)
        } else {
            Log.e("error", result?.message() ?: "")
            null
        }
    }

    suspend fun getSearchTracks(query: String, limit: Int, offset: Int, type: String, context: Context): List<Track>? {
        var result: Response<SearchTracksObj>? = null
        try {
            result = ApiRepository.getCustomApiService().getSearch(query, type, limit, offset)
        } catch (exception: SocketTimeoutException) {
            when (exception) {
                is UserNotAuthorizedException -> (context as MainActivity).authorizeUser()
                else -> (context as MainActivity).viewModel.showError()
            }
        }
        return if (result?.isSuccessful == true) {
            result.body()?.tracksObj?.tracks?.toMutableList()

        } else {
            Log.e("error", result?.message() ?: "error")
            null
        }
    }

    suspend fun getTopGenresSeeds(topArtists: List<Artist>, context: Context) : List<String>{
        val response = getAvailableGenreSeeds(context)
        val availableGenres = response?.seeds?.toList()
        val genreMap = HashMap<String,Int>()
        for (artist in topArtists){
            for (genre in artist.genres!!) {
                genreMap[genre] = if (genreMap.containsKey(genre)) genreMap.getValue(genre) + 1 else 1
            }
        }
        val sortedGenres = ArrayList<String>()
        genreMap.entries.sortedByDescending{it.value}.forEach{sortedGenres.add(it.key)}
        return sortedGenres.filter { availableGenres?.contains(it) == true}.take(5)
    }

    private fun getTracksUris(tracks: MutableList<Track>): String {
        return tracks.joinToString(separator = ",") {it.uri.toString()}
    }

    private fun getArtistsIdsFromArtists(artists: MutableList<Artist>) : String {
        return artists.joinToString(separator = ",") { it.artistId }
    }

    private fun getGenresSeedsString(genres: MutableList<String>) : String {
        return genres.joinToString(separator = ",")
    }

    private fun getArtistsIdsFromTracks(tracks : List<Track>) : String{
        return tracks.joinToString(separator = ",") { it.artists[0].artistId }
    }

    private fun getTracksSeedsString(tracks: List<Track>): String {
        return tracks.joinToString(separator = ",") { it.trackId }
    }

    suspend fun getAvailableGenreSeeds(context: Context): GenreSeeds?{
        var result : Response<GenreSeeds>? = null
        try {
            result = ApiRepository.getCustomApiService().getAvailableGenres()
        } catch (exception: SocketTimeoutException) {
            when (exception) {
                is UserNotAuthorizedException -> (context as MainActivity).authorizeUser()
                else -> (context as MainActivity).viewModel.showError()
            }
        }
        return if (result?.isSuccessful == true) {
            result.body()
        } else {
            Log.e("error", result?.message() ?: "")
            null
        }
    }

    suspend fun getArtistWithGenres(tracks: MutableList<Track>, context: Context): ArtistsList? {
        var result : Response<ArtistsList>? = null
        try {
            result = ApiRepository.getCustomApiService().getArtists(ids = getArtistsIdsFromTracks(tracks))
        } catch (exception: SocketTimeoutException) {
            when (exception) {
                is UserNotAuthorizedException -> (context as MainActivity).authorizeUser()
                else -> (context as MainActivity).viewModel.showError()
            }
        }
        return if (result?.isSuccessful == true) {
            result.body()
        } else {
            Log.e("error", result?.message() ?: "")
            null
        }
    }

    suspend fun getArtistsDetail(artists: MutableList<Artist>, context: Context): ArtistsList? {
        var result : Response<ArtistsList>? = null
        try {
            result = ApiRepository.getCustomApiService().getArtists(ids = getArtistsIdsFromArtists(artists))
        } catch (exception: SocketTimeoutException) {
            when (exception) {
                is UserNotAuthorizedException -> (context as MainActivity).authorizeUser()
                else -> (context as MainActivity).viewModel.showError()
            }
        }
        return if (result?.isSuccessful == true) {
            result.body()
        } else {
            Log.e("error", result?.message() ?: "")
            null
        }
    }

    suspend fun getRecommendsBasedOnArtists(artists: MutableList<Artist>, context: Context) : RecommendedTracks?{
        var result : Response<RecommendedTracks>? = null
        try {
            result = ApiRepository.getCustomApiService().getRecommendationsResp(
                seed_artist = getArtistsIdsFromArtists(artists),
                seed_tracks = "",
                seed_genres = "",
                limit = Config.recommendedCount
            )
        } catch (exception: SocketTimeoutException) {
            when (exception) {
                is UserNotAuthorizedException -> (context as MainActivity).authorizeUser()
                else -> (context as MainActivity).viewModel.showError()
            }
        }
        return if (result?.isSuccessful == true) {
            result.body()
        } else {
            Log.e("error", result?.message() ?: "error")
            null
        }
    }

    suspend fun getRecommendsBasedOnGenres(genres: MutableList<String>, context: Context) : RecommendedTracks?{
        var result : Response<RecommendedTracks>? = null
        try {
            result = ApiRepository.getCustomApiService().getRecommendationsResp(
                seed_artist = "",
                seed_tracks = "",
                seed_genres = getGenresSeedsString(genres),
                limit = Config.recommendedCount
            )
        } catch (exception: SocketTimeoutException) {
            when (exception) {
                is UserNotAuthorizedException -> (context as MainActivity).authorizeUser()
                else -> (context as MainActivity).viewModel.showError()
            }
        }
        return if (result?.isSuccessful == true) {
            result.body()
        } else {
            Log.e("error", result?.message() ?: "error")
            null
        }
    }

    suspend fun getRecommendsBasedOnTracks(tracks: MutableList<Track>, context: Context) : RecommendedTracks?{
        var result : Response<RecommendedTracks>? = null
        try {
            result = ApiRepository.getCustomApiService().getRecommendationsResp(
                seed_artist = "",
                seed_tracks = getTracksSeedsString(tracks),
                seed_genres = "",
                limit = Config.recommendedCount
            )
        } catch (exception: SocketTimeoutException) {
            when (exception) {
                is UserNotAuthorizedException -> (context as MainActivity).authorizeUser()
                else -> (context as MainActivity).viewModel.showError()
            }
        }
        return if (result?.isSuccessful == true) {
            result.body()
        } else {
            Log.e("error", result?.message() ?: "error")
            null
        }
    }

    suspend fun getRecommendsCombined(seeds: MutableList<Pair<RecommendSeedType, String>>, context: Context) : RecommendedTracks?{
        val artistSeeds = seeds.filter { it.first == RecommendSeedType.ARTIST }.map { it.second }
        val trackSeeds = seeds.filter { it.first == RecommendSeedType.TRACK }.map { it.second }
        val genreSeeds = seeds.filter { it.first == RecommendSeedType.GENRE }.map { it.second }
        var result : Response<RecommendedTracks>? = null
        try {
            result = ApiRepository.getCustomApiService().getRecommendationsResp(
                seed_artist = artistSeeds.joinToString(separator = ","),
                seed_tracks = trackSeeds.joinToString(separator = ","),
                seed_genres = genreSeeds.joinToString(separator = ","),
                limit = Config.recommendedCount
            )
        } catch (exception: SocketTimeoutException) {
            when (exception) {
                is UserNotAuthorizedException -> (context as MainActivity).authorizeUser()
                else -> (context as MainActivity).viewModel.showError()
            }
        }
        return if (result?.isSuccessful == true) {
            result.body()
        } else {
            Log.e("error", result?.message() ?: "")
            null
        }
    }
}