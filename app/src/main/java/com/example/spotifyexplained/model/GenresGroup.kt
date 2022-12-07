package com.example.spotifyexplained.model

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


