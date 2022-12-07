package com.example.spotifyexplained.model

data class LineDetailGenreInfo(
    var sourceArtist: Artist?,
    var targetArtist: Artist?,
    var genres: List<String>,
)
