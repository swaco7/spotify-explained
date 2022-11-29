package com.example.spotifyexplained.model

import androidx.room.Embedded
import com.google.gson.annotations.SerializedName
import com.highsoft.highcharts.common.hichartsclasses.HIMarker

data class TrackAudioFeatures(
    @Embedded
    val track: Track,
    @Embedded
    var features: AudioFeatures
)
