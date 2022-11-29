package com.example.spotifyexplained.model

import com.highsoft.highcharts.common.hichartsclasses.HIDataLabels
import com.highsoft.highcharts.common.hichartsclasses.HIMarker

data class GenresRelation(
    val first: String,
    val second: String,
    var value: Int
) {

    override fun hashCode(): Int {
        var result = first.hashCode()
        result = 31 * result + second.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GenresRelation
        if ((first == other.first && second == other.second) ||
            (first == other.second && second == other.first)) return true
        return false
    }
}
