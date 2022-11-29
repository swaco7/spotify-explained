package com.example.spotifyexplained.model

import android.text.Html
import com.highsoft.highcharts.common.hichartsclasses.HIMarker

class D3Link(
    val source: String,
    val target: String,
    val value: Int,
    val color: String,
    val type: LinkType
) {
    override fun toString(): String {
        return " {\"source\": \"${Html.escapeHtml(source)}\", \"target\": \"${Html.escapeHtml(target)}\", \"value\": $value, \"color\": \"$color\", \"type\": \"${type.name}\"}"
    }
}

