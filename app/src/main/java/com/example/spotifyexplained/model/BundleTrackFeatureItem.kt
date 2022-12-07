package com.example.spotifyexplained.model

import com.example.spotifyexplained.model.enums.AudioFeatureType
import com.example.spotifyexplained.model.enums.BundleItemType

data class BundleTrackFeatureItem(
    var track: Track?,
    var audioFeatures: List<Pair<String, Double?>>,
    var bundleItemType: BundleItemType,
    var audioFeatureType: AudioFeatureType?
)
