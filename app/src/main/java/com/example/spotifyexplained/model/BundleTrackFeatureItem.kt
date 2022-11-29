package com.example.spotifyexplained.model

import com.google.gson.annotations.SerializedName
import com.highsoft.highcharts.common.hichartsclasses.HIMarker

data class BundleTrackFeatureItem(
    var track: Track?,
    var audioFeatures: List<Pair<String, Double?>>,
    var bundleItemType: BundleItemType,
    var audioFeatureType: AudioFeatureType?
)
