package com.example.spotifyexplained

import com.example.spotifyexplained.general.Helper
import com.example.spotifyexplained.model.*
import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import org.mockito.Mock
import org.mockito.Mockito

class HelperFeaturesUnitTest {
    private val mockTrack = Mockito.mock(Track::class.java)
    private val mockFeaturesRec = TrackAudioFeatures(
        track = mockTrack,
        features = AudioFeatures(
            featuresId = "f1",
            featuresValues = listOf(1.0,0.9,0.8,0.7,0.6,0.5,0.4,0.3,0.2)
        )
    )
    private val mockFeaturesTop = TrackAudioFeatures(
        track = mockTrack,
        features = AudioFeatures(
            featuresId = "f2",
            featuresValues = listOf(1.0,0.9,0.8,0.7,0.6,0.5,0.4,0.3,0.2)
        )
    )
    private val mockFeaturesTopDiff = TrackAudioFeatures(
        track = mockTrack,
        features = AudioFeatures(
            featuresId = "f3",
            featuresValues = listOf(0.5,0.5,0.5,0.5,0.6,0.9,0.9,0.9,0.9)
        )
    )
    private val recommendedTracks = listOf(mockFeaturesRec)
    private val topTracks = mutableListOf(mockFeaturesTop)
    private val topTracksDiff = mutableListOf(mockFeaturesTopDiff)

    @Test
    fun getFeaturesEdges_allSimilar() {
        val result = Helper.getFeaturesEdges(recommendedTracks, topTracks)
        assertEquals(1, result.size)
    }

    @Test
    fun getFeaturesEdges_notEnoughSimilar() {
        val result = Helper.getFeaturesEdges(recommendedTracks, topTracksDiff)
        assertEquals(0, result.size)
    }

    @Test
    fun featuresSimilar () {
        assertEquals(0.9, Helper.featuresSimilar(1.0, 0.9), 0.001)
    }
    @Test
    fun featuresSimilarReversed () {
        assertEquals(0.5, Helper.featuresSimilar(0.5, 1.0), 0.001)
    }
    @Test
    fun featuresSimilarNillOne () {
        assertEquals(0.0, Helper.featuresSimilar(1.0, 0.0), 0.001)
    }
    @Test
    fun featuresSimilarNillBoth () {
        assertEquals(0.0, Helper.featuresSimilar(0.0, 0.0), 0.001)
    }
}