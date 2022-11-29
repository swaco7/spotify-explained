package com.example.spotifyexplained.model

import com.google.gson.annotations.SerializedName
import com.highsoft.highcharts.common.hichartsclasses.HIMarker

data class BundleLineInfo(
    var sourceArtist: Artist? = null,
    var targetArtist: Artist? = null,
    var genres: String? = null,
    var genreColor: ArrayList<Float>? = null,
    var sourceTrack: TrackAudioFeatures? = null,
    var targetTrack: TrackAudioFeatures? = null,
    var features: Pair<AudioFeatureType, Double>? = null,
    var sToT: Int? = null,
    var tToS: Int? = null,
    var linkType: LinkType
)
