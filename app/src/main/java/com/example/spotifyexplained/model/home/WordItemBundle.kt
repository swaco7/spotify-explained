package com.example.spotifyexplained.model.home

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
