package com.example.spotifyexplained.model

import android.text.Html
import com.highsoft.highcharts.common.hichartsclasses.HIMarker

class D3LinkDistance(
    val source: String,
    val target: String,
    val value: Int,
    val distance: Int
) {
    override fun toString(): String {
        return " {\"source\": \"${Html.escapeHtml(source)}\", \"target\": \"${Html.escapeHtml(target)}\", \"value\": $value, \"distance\": \"$distance\"}"
    }
}
