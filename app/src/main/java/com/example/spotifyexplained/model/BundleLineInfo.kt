package com.example.spotifyexplained.model

import com.example.spotifyexplained.model.enums.AudioFeatureType
import com.example.spotifyexplained.model.enums.LinkType

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
