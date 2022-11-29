package com.example.spotifyexplained.model

import android.text.Html
import com.highsoft.highcharts.common.hichartsclasses.HIMarker

class D3Node(
    val id: String,
    val group: Int,
    val radius: Int,
    val color: String
) {
    override fun toString(): String {
        return "{\"id\": \"${Html.escapeHtml(id)}\", \"group\": $group, \"radius\": $radius, \"color\": \"$color\"}"
    }
}

