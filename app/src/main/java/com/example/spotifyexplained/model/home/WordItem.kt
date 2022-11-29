package com.example.spotifyexplained.model.home

import android.text.Html
import com.google.gson.annotations.SerializedName
import com.highsoft.highcharts.common.hichartsclasses.HIMarker

data class WordItem(
    var title: String,
    var size: Int
) {
    override fun toString(): String {
        return " {\"text\": \"${Html.escapeHtml(title)}\", \"size\": $size}"
    }
}
