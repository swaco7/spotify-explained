package com.example.spotifyexplained.model

data class TrackValue (
    var trackId: String? = null,
    var name: String,
    var artistName: String? = null,
    var imageUrl: String? = null,
    val value: Double
    )
