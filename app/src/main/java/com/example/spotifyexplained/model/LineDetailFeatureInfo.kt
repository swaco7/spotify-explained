package com.example.spotifyexplained.model

import com.example.spotifyexplained.model.enums.AudioFeatureType

data class LineDetailFeatureInfo(
    var sourceTrack: TrackAudioFeatures?,
    var targetTrack: TrackAudioFeatures?,
    var features: List<Pair<AudioFeatureType, Double>>?
)
