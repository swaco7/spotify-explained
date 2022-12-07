package com.example.spotifyexplained.model

data class LineDetailInfo(
    var sourceArtist: Artist?,
    var targetArtist: Artist?,
    var sToT: Int?,
    var tToS: Int?,
)
