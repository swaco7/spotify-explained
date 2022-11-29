package com.example.spotifyexplained.model

import com.google.gson.annotations.SerializedName

data class SimilarTrack(
    val name : String,
    val color: String,
    val similarity: Double,
    val track: TrackAudioFeatures? = null,
    )
