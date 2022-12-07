package com.example.spotifyexplained.model

import com.example.spotifyexplained.model.enums.AudioFeatureType

data class GraphFeaturesLink(
    val tracks : Pair<Track,Track>,
    val features: MutableList<Pair<AudioFeatureType, Double>>
)
