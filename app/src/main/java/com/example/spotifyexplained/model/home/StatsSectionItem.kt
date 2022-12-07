package com.example.spotifyexplained.model.home

data class StatsSectionItem(
    var trackId: String? = null,
    var name: String,
    var value: Double,
    var artistName: String? = null,
    var imageUrl: String? = null
)
