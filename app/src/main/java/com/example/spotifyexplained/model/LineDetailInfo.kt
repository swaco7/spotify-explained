package com.example.spotifyexplained.model

import com.google.gson.annotations.SerializedName
import com.highsoft.highcharts.common.hichartsclasses.HIMarker

data class LineDetailInfo(
    var sourceArtist: Artist?,
    var targetArtist: Artist?,
    var sToT: Int?,
    var tToS: Int?,
)
