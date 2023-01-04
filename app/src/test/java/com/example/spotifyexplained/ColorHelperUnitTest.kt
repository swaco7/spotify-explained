package com.example.spotifyexplained

import com.example.spotifyexplained.general.ColorHelper
import com.example.spotifyexplained.general.Helper
import com.example.spotifyexplained.model.*
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import org.mockito.Mock
import org.mockito.Mockito

class ColorHelperUnitTest {
    lateinit var availableSeeds : GenreSeeds
    lateinit var genresList: List<Array<String>>
    lateinit var genresGroups : MutableList<GenresGroup>

    @Test
    fun genreGroups_sameSeed_diferrentForm() {
        availableSeeds = GenreSeeds(seeds = arrayOf("pop", "indie"))
        genresList = listOf(arrayOf("pop", "electropop", "dance pop"))
        val result = ColorHelper.prepareGenresGroups(availableSeeds, genresList)
        assertEquals(1, result.size)
    }

    @Test
    fun genreGroups_availableSeed() {
        availableSeeds = GenreSeeds(seeds = arrayOf("pop", "indie", "dance pop"))
        genresList = listOf(arrayOf("pop", "electropop", "dance pop"), arrayOf("pop", "electropop"))
        val result = ColorHelper.prepareGenresGroups(availableSeeds, genresList)
        assertEquals(2, result.size)
    }

    @Test
    fun genreGroups_customSeed() {
        availableSeeds = GenreSeeds(seeds = arrayOf("pop", "indie"))
        genresList = listOf(arrayOf("pop", "electropop", "epic music"))
        val result = ColorHelper.prepareGenresGroups(availableSeeds, genresList)
        assertEquals(2, result.size)
    }

    @Test
    fun genreGroups_order() {
        availableSeeds = GenreSeeds(seeds = arrayOf("pop", "indie"))
        genresList = listOf(arrayOf("indie-folk", "indiecoustica", "indie"))
        val result = ColorHelper.prepareGenresGroups(availableSeeds, genresList)
        assertEquals(1, result.size)
    }

    @Test
    fun removeExtraGenreGroups() {
        genresGroups = mutableListOf(GenresGroup("alternative", mutableListOf("alternative pop", "alternative dancepop")), GenresGroup("alternative indie", mutableListOf("alternative indie")))
        ColorHelper.removeExtraGenreGroups(genresGroups)
        assertEquals(1, genresGroups.size)
    }
}