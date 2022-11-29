package com.example.spotifyexplained.model.home

import com.google.gson.annotations.SerializedName
import com.highsoft.highcharts.common.hichartsclasses.HIMarker

data class StatsSectionItem(
    var trackId: String? = null,
    var name: String,
    var value: Double,
    var artistName: String? = null,
    var imageUrl: String? = null
)
