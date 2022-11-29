package com.example.spotifyexplained.model

data class GraphFeaturesLink(
    val tracks : Pair<Track,Track>,
    val features: MutableList<Pair<AudioFeatureType, Double>>
)
