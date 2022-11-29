package com.example.spotifyexplained.model

import com.google.gson.annotations.SerializedName
import com.highsoft.highcharts.common.hichartsclasses.HIMarker

data class BundleGraphItem(
    var track: Track?,
    var artist: Artist?,
    var genreColors: List<GenreColor>?,
    var genre: String?,
    var bundleItemType: BundleItemType
)
