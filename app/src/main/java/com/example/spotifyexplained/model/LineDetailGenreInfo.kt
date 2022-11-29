package com.example.spotifyexplained.model

import com.google.gson.annotations.SerializedName
import com.highsoft.highcharts.common.hichartsclasses.HIMarker

data class LineDetailGenreInfo(
    var sourceArtist: Artist?,
    var targetArtist: Artist?,
    var genres: List<String>,
)
