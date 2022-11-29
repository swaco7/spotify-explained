package com.example.spotifyexplained.services

import android.content.ClipDescription
import android.content.Context
import android.util.Log
import com.example.spotifyexplained.model.*
import kotlinx.coroutines.delay
import retrofit2.Response

object ApiHelper {
    suspend fun getUserTopTracksNew(limit: Int): Response<TopItemsTrack> {
        return ApiRepository.getCustomApiService().getUserTopTracksResp(limit = limit)
    }

    suspend fun getUserTopTracks(limit: Int): MutableList<Track>? {
        val result = ApiRepository.getCustomApiService().getUserTopTracksResp(limit = limit)
        return if (result.isSuccessful) {
            val response = result.body()
            response!!.items.toMutableList()
        } else {
            Log.e("error", result.message())
            if (result.message() == "Unauthorized") {
                Log.e("error", result.message())
            }
            null
        }
    }

    suspend fun addTracksToPlaylist(playlistId: String, tracks: MutableList<Track>): PlaylistAddResult? {
        val result = ApiRepository.getCustomApiService().postTracksToPlaylist(playlistId, getTracksUris(tracks))
        return if (result.isSuccessful) {
            val response = result.body()
            response
        } else {
            Log.e("error", result.message())
            if (result.message() == "Unauthorized") {
                Log.e("error", result.message())
            }
            null
        }
    }

    suspend fun createPlaylist(name: String, description: String, isPublic: Boolean, isCollaborative: Boolean): PlaylistDataReturned? {
        val result = ApiRepository.getCustomApiService().postCreatePlaylist(SessionManager.getUserId()!!, PlaylistData(name, isPublic, isCollaborative, description))
        return if (result.isSuccessful) {
            val response = result.body()
            response
        } else {
            Log.e("error", result.message())
            if (result.message() == "Unauthorized") {
                Log.e("error", result.message())
            }
            null
        }
    }

    suspend fun getProfile(): UserProfile? {
        val result = ApiRepository.getCustomApiService().getProfile()
        return if (result.isSuccessful) {
            val response = result.body()
            response
        } else {
            Log.e("error", result.message())
            null
        }
    }

    suspend fun getUserSavedTracks(limit : Int): MutableList<Track>?{
        val result = ApiRepository.getCustomApiService().getUserSavedTracks(limit = limit)
        return if (result.isSuccessful) {
            val response = result.body()
            response!!.items.map { it.track }.toMutableList()
        } else {
            Log.e("error", result.message())
            null
        }
    }

    suspend fun getUserTopArtists(limit : Int) : MutableList<Artist>?{
        val result = ApiRepository.getCustomApiService().getUserTopArtists(limit = limit)
        return if (result.isSuccessful) {
            val response = result.body()
            response!!.items.toMutableList()
        } else {
            Log.e("error", result.message())
            null
        }
    }

    suspend fun getRelatedArtists(artistId : String) : MutableList<Artist>?{
        val result = ApiRepository.getCustomApiService().getRelatedArtists(id = artistId)
        return if (result.isSuccessful) {
            val response = result.body()
            response!!.artists.toMutableList()
        } else {
            Log.e("error", result.message())
            //delay(500)
            //getRelatedArtists(artistId)
            null
        }
    }

    suspend fun getArtistTopTracks(artistId : String) : MutableList<Track>?{
        val result = ApiRepository.getCustomApiService().getArtistTopTracks(id = artistId, market = "SK")
        return if (result.isSuccessful) {
            val response = result.body()
            response!!.tracks.toMutableList()
        } else {
            Log.e("error", result.message())
            //delay(500)
            //getArtistTopTracks(artistId)
            null
        }
    }

    suspend fun getTracksAudioFeatures(tracks: List<Track>): List<TrackAudioFeatures>? {
        val result = ApiRepository.getCustomApiService().getTracksAudioFeatures(ids = getTracksSeedsString(tracks))
        return if (result.isSuccessful) {
            val response = result.body()
            val list = ArrayList<TrackAudioFeatures>()
            for (trackFeatures in response!!.audio_features) {
                if (trackFeatures != null) {
                    list.add(
                        TrackAudioFeatures(
                            tracks.find { it.trackId == trackFeatures.featuresId }!!,
                            trackFeatures
                        )
                    )
                }
            }
            list
        } else {
            Log.e("error", result.message())
            null
        }
    }

    suspend fun getTracks(trackId: String) : Track? {
        val result = ApiRepository.getCustomApiService().getTracks(ids = trackId)
        return if (result.isSuccessful) {
            result.body()?.tracks?.get(0)
        } else {
            Log.e("error", result.message())
            null
        }
    }

    suspend fun getSearchTracks(query: String, limit: Int, offset: Int, type: String): List<Track>?{
        var result : Response<SearchTracksObj>? = null
        try {
            result = ApiRepository.getCustomApiService().getSearch(query, type, limit, offset)
        } catch (exception: NoConnectivityException){
            Log.e("result", "here")
        }
        if (result != null) {
            return if (result.isSuccessful) {
                result.body()?.tracksObj?.tracks?.toMutableList()
            } else {
                Log.e("error", result.message())
                null
            }
        } else {
            return null
        }
    }

    suspend fun getTracksAudioFeaturesContext(ids: String, tracks: MutableList<Track>, context: Context): MutableList<TrackAudioFeatures>? {
        val result = ApiRepository.getCustomApiService().getTracksAudioFeatures(ids = ids)
        return if (result.isSuccessful) {
            val response = result.body()
            val list = ArrayList<TrackAudioFeatures>()
            for (trackFeatures in response!!.audio_features) {
                if (trackFeatures == null) {
                    Log.e("response", response.audio_features.toString())
                    continue
                }
                val track = tracks.firstOrNull{ it.trackId == trackFeatures.featuresId }
                list.add(TrackAudioFeatures(tracks.find { it.trackId == trackFeatures.featuresId }!!,trackFeatures))
            }
            list
        } else {
            showError(context, result.message())
            Log.e("error", result.message())
            null
        }
    }

    suspend fun getTopGenresSeeds(topArtists: List<Artist>) : List<String>{
        val response  = getAvailableGenreSeeds()
        val availableGenres = response!!.seeds.toList()
        val genreMap = HashMap<String,Int>()
        for (artist in topArtists){
            for (genre in artist.genres!!) {
                genreMap[genre] = if (genreMap.containsKey(genre)) genreMap.getValue(genre) + 1 else 1
            }
        }
        val sortedGenres = ArrayList<String>()
        genreMap.entries.sortedByDescending{it.value}.forEach{sortedGenres.add(it.key)}
        return sortedGenres.filter { availableGenres.contains(it) }.take(5)
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

    fun getTracksSeedsString(tracks: List<Track>): String {
        return tracks.joinToString(separator = ",") { it.trackId }
    }

    suspend fun getAvailableGenreSeeds(): GenreSeeds?{
        val result = ApiRepository.getCustomApiService().getAvailableGenres()
        return if (result.isSuccessful) {
            result.body()!!
        } else {
            Log.e("error", result.message())
            null
        }
    }

    suspend fun getArtistWithGenres(tracks: MutableList<Track>): ArtistsList? {
        val result = ApiRepository.getCustomApiService().getArtists(ids = getArtistsIdsFromTracks(tracks))
        return if (result.isSuccessful) {
            result.body()
        } else {
            Log.e("error", result.message())
            null
        }
    }

    suspend fun getArtistsDetail(artists: MutableList<Artist>): ArtistsList? {
        val result = ApiRepository.getCustomApiService().getArtists(ids = getArtistsIdsFromArtists(artists))
        return if (result.isSuccessful) {
            result.body()
        } else {
            Log.e("error", result.message())
            null
        }
    }

    suspend fun getRecommendsBasedOnArtists(artists: MutableList<Artist>) : RecommendedTracks?{
        val result = ApiRepository.getCustomApiService().getRecommendationsResp(
            seed_artist = getArtistsIdsFromArtists(artists),
            seed_tracks = "",
            seed_genres = "",
            limit = Constants.recommendedCount
        )
        return if (result.isSuccessful) {
            result.body()
        } else {
            Log.e("error", result.message())
            null
        }
    }

    suspend fun getRecommendsBasedOnGenres(genres: MutableList<String>) : RecommendedTracks?{
        val result = ApiRepository.getCustomApiService().getRecommendationsResp(
            seed_artist = "",
            seed_tracks = "",
            seed_genres = getGenresSeedsString(genres),
            limit = Constants.recommendedCount
        )
        return if (result.isSuccessful) {
            result.body()
        } else {
            Log.e("error", result.message())
            null
        }
    }

    suspend fun getRecommendsBasedOnTracks(tracks: MutableList<Track>) : RecommendedTracks?{
        val result = ApiRepository.getCustomApiService().getRecommendationsResp(
            seed_artist = "",
            seed_tracks = getTracksSeedsString(tracks),
            seed_genres = "",
            limit = Constants.recommendedCount
        )
        return if (result.isSuccessful) {
            result.body()
        } else {
            Log.e("error", result.message())
            null
        }
    }

    suspend fun getRecommendsCombined(seeds: MutableList<Pair<RecommendSeedType, String>>) : RecommendedTracks?{
        val artistSeeds = seeds.filter { it.first == RecommendSeedType.ARTIST }.map { it.second }
        val trackSeeds = seeds.filter { it.first == RecommendSeedType.TRACK }.map { it.second }
        val genreSeeds = seeds.filter { it.first == RecommendSeedType.GENRE }.map { it.second }
        val result = ApiRepository.getCustomApiService().getRecommendationsResp(
            seed_artist = artistSeeds.joinToString(separator = ","),
            seed_tracks = trackSeeds.joinToString(separator = ","),
            seed_genres = genreSeeds.joinToString(separator = ","),
            limit = Constants.recommendedCount
        )
        return if (result.isSuccessful) {
            result.body()
        } else {
            Log.e("error", result.message())
            null
        }
    }

    private fun showError(context: Context, error: String ) {
        android.app.AlertDialog.Builder(context)
            .setMessage(error)
            .setPositiveButton("OK"
            ) { p0, p1 -> p0.cancel() }
            .show()
    }
}