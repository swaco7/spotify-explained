package com.example.spotifyexplained.model.home

import android.text.Html

data class WordItem(
    var title: String,
    var size: Int
) {
    override fun toString(): String {
        return " {\"text\": \"${title}\", \"size\": $size}"
    }
}
