package com.example.spotifyexplained.model

import android.text.Html

data class DataUnit(
    val name: String,
    val value: Double,
    val group: Int,
) {
    override fun toString(): String {
        return "{\"name\": \"${Html.escapeHtml(name)}\", \"value\": ${(value*100).toInt()}, \"group\": $group}"
    }
}

