package com.example.spotifyexplained.model

import com.google.gson.annotations.SerializedName
import com.highsoft.highcharts.common.hichartsclasses.HIMarker

data class LineDetailFeatureInfo(
    var sourceTrack: TrackAudioFeatures?,
    var targetTrack: TrackAudioFeatures?,
    var features: List<Pair<AudioFeatureType, Double>>?
)
