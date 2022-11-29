package com.example.spotifyexplained.model.home

import com.google.gson.annotations.SerializedName
import com.highsoft.highcharts.common.hichartsclasses.HIMarker

data class PageItem(
    var title: String,
    var content: String,
    var pageType: PageType
)
