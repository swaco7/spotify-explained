package com.example.spotifyexplained.model

import com.example.spotifyexplained.model.enums.BundleItemType

data class BundleGraphItem(
    var track: Track?,
    var artist: Artist?,
    var genreColors: List<GenreColor>?,
    var genre: String?,
    var bundleItemType: BundleItemType
)
