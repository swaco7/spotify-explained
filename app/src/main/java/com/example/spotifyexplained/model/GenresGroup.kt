package com.example.spotifyexplained.model

import com.highsoft.highcharts.common.hichartsclasses.HIDataLabels
import com.highsoft.highcharts.common.hichartsclasses.HIMarker

data class GenresGroup(
    var name: String,
    val items: MutableList<String>
) {

    fun contains(genreGroup: GenresGroup): Boolean {
        return items.containsAll(genreGroup.items)
    }

    fun sharePartOfName(genreGroup: GenresGroup): Boolean {
        val nameSplit = name.split(' ').toSet()
        val groupNameSplit = genreGroup.name.split(' ').toSet()
        return nameSplit.intersect(groupNameSplit).joinToString(separator = "_").length > 6
    }
}


