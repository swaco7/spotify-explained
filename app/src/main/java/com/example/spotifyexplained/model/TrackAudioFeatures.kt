package com.example.spotifyexplained.model

import androidx.room.Embedded

data class TrackAudioFeatures(
    @Embedded
    val track: Track,
    @Embedded
    var features: AudioFeatures
)
