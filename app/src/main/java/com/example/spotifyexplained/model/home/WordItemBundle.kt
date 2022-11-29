package com.example.spotifyexplained.model.home

import android.text.Html
import com.google.gson.annotations.SerializedName
import com.highsoft.highcharts.common.hichartsclasses.HIMarker

data class WordItemBundle(
    var title: String,
    var content: MutableList<WordItem>)
//) {
//    override fun toString(): String {
//
//        for (item in content){
//            return "{\"id\": \"${Html.escapeHtml(id)}\", \"group\": $group, \"radius\": $radius, \"color\": \"$color\"}"
//        }
//    }
//}
