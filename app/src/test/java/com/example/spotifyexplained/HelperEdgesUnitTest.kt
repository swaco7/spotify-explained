package com.example.spotifyexplained

import com.example.spotifyexplained.general.Helper
import com.example.spotifyexplained.model.*
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import org.mockito.Mock
import org.mockito.Mockito

class HelperEdgesUnitTest {
    private val mockAlbum = Mockito.mock(Album::class.java)
    lateinit var artist1 : Artist
    lateinit var artist2 : Artist
    lateinit var artist3 : Artist
    lateinit var artist4 : Artist
    lateinit var mockTrack : Track
    lateinit var recommendedTracks : List<Track>
    lateinit var topArtists : List<Artist>

    @Before
    fun setup(){
        artist1 = Artist(
            artistId = "art1",
            artistName = "artist1",
            artistPopularity = 50,
            genres = arrayOf("pop", "rock", "indie"),
            images = arrayOf(),
            related_artists = listOf()
        )
        artist2 = Artist(
            artistId = "art2",
            artistName = "artist2",
            artistPopularity = 50,
            genres = arrayOf("pop", "rock", "indie"),
            images = arrayOf(),
            related_artists = listOf()
        )
        artist3 = Artist(
            artistId = "art3",
            artistName = "artist3",
            artistPopularity = 50,
            genres = arrayOf("pop", "rock", "indie"),
            images = arrayOf(),
            related_artists = listOf()
        )
        artist4 = Artist(
            artistId = "art4",
            artistName = "artist4",
            artistPopularity = 50,
            genres = arrayOf("pop", "rock", "indie"),
            images = arrayOf(),
            related_artists = listOf()
        )

        mockTrack = Track(trackId = "t1",
            trackName = "track1",
            album = mockAlbum,
            artists = arrayOf(artist1),
            trackGenres = arrayOf("pop"),
            track_related_artists = listOf(),
            popularity = 50
        )
    }


    @Test
    fun getEdgesRelated_Intersect() {
        artist1.related_artists = listOf(artist3)
        artist2.related_artists = listOf(artist1)
        artist3.related_artists = listOf()
        artist4.related_artists = listOf(artist2)
        recommendedTracks = listOf(mockTrack)
        topArtists = listOf(artist3, artist4)
        val result = Helper.getEdges(recommendedTracks, topArtists)
        assertEquals(1, result.size)
    }

    @Test
    fun getEdgesRelated_Switched() {
        artist1.related_artists = listOf(artist3)
        artist2.related_artists = listOf(artist1)
        artist3.related_artists = listOf()
        artist4.related_artists = listOf(artist1)
        recommendedTracks = listOf(mockTrack)
        topArtists = listOf(artist3, artist4)
        val result = Helper.getEdges(recommendedTracks, topArtists)
        assertEquals(2, result.size)
    }

    @Test
    fun getEdgesRelated_Distinct() {
        artist1.related_artists = listOf(artist3)
        artist2.related_artists = listOf(artist1)
        artist3.related_artists = listOf(artist1)
        artist4.related_artists = listOf(artist2)
        recommendedTracks = listOf(mockTrack)
        topArtists = listOf(artist3, artist4)
        val result = Helper.getEdges(recommendedTracks, topArtists)
        assertEquals(1, result.size)
    }

    @Test
    fun getEdgesGenres_Intersect() {
        artist1.genres = arrayOf("pop")
        artist2.genres = arrayOf("pop", "rock")
        recommendedTracks = listOf(mockTrack)
        topArtists = listOf(artist2)
        val result = Helper.getGenreEdges(recommendedTracks, topArtists)
        assertEquals(1, result.size)
    }

    @Test
    fun getEdgesGenres_empty() {
        artist1.genres = arrayOf()
        artist2.genres = arrayOf()
        recommendedTracks = listOf(mockTrack)
        topArtists = listOf(artist2)
        val result = Helper.getGenreEdges(recommendedTracks, topArtists)
        assertEquals(0, result.size)
    }

    @Test
    fun getEdgesGenres_null() {
        artist1.genres = null
        artist2.genres = null
        recommendedTracks = listOf(mockTrack)
        topArtists = listOf(artist2)
        val result = Helper.getGenreEdges(recommendedTracks, topArtists)
        assertEquals(0, result.size)
    }

    @Test
    fun getEdgesGenres_None() {
        artist1.genres = arrayOf("pop")
        artist2.genres = arrayOf("indie", "rock")
        recommendedTracks = listOf(mockTrack)
        topArtists = listOf(artist2)
        val result = Helper.getGenreEdges(recommendedTracks, topArtists)
        assertEquals(0, result.size)
    }

    @Test
    fun getEdgesGenres_Distinct() {
        artist1.genres = arrayOf("pop")
        artist2.genres = arrayOf("pop", "indie")
        artist3.genres = arrayOf("pop", "rock")
        recommendedTracks = listOf(mockTrack)
        topArtists = listOf(artist2, artist3)
        val result = Helper.getGenreEdges(recommendedTracks, topArtists)
        assertEquals(2, result.size)
    }


    @Test
    fun getTopArtistsEdges_duplicate() {
        artist1.related_artists = listOf(artist3)
        artist2.related_artists = listOf(artist1)
        artist3.related_artists = listOf(artist1)
        artist4.related_artists = listOf()
        topArtists = listOf(artist1, artist2, artist3, artist4)
        val result = Helper.getTopArtistsEdges(topArtists)
        assertEquals(2, result.size)
    }

    @Test
    fun getTopArtistsEdges_empty() {
        artist1.related_artists = listOf(artist3)
        artist2.related_artists = listOf(artist4)
        artist3.related_artists = listOf(artist1)
        artist4.related_artists = listOf()
        topArtists = listOf(artist1, artist2)
        val result = Helper.getTopArtistsEdges(topArtists)
        assertEquals(0, result.size)
    }

    @Test
    fun getTopArtistsEdgesGenres_Intersect() {
        artist1.genres = arrayOf("pop")
        artist2.genres = arrayOf("pop", "rock")
        artist3.genres = arrayOf()
        recommendedTracks = listOf(mockTrack)
        topArtists = listOf(artist1, artist2, artist3)
        val result = Helper.getTopArtistsGenresEdges(topArtists)
        assertEquals(1, result.size)
    }

    @Test
    fun getTopArtistsEdgesGenres_None() {
        artist1.genres = arrayOf("pop")
        artist2.genres = arrayOf("indie", "rock")
        recommendedTracks = listOf(mockTrack)
        topArtists = listOf(artist1, artist2)
        val result = Helper.getTopArtistsGenresEdges(topArtists)
        assertEquals(0, result.size)
    }

    @Test
    fun getTopArtistsEdgesGenres_null() {
        artist1.genres = null
        artist2.genres = arrayOf("pop", "indie")
        artist3.genres = arrayOf("pop", "rock")
        recommendedTracks = listOf(mockTrack)
        topArtists = listOf(artist1, artist2, artist3)
        val result = Helper.getTopArtistsGenresEdges(topArtists)
        assertEquals(1, result.size)
    }
}