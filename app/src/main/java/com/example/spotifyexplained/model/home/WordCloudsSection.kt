package com.example.spotifyexplained.model.home

data class WordCloudsSection(
    override var type: HomeSectionType,
    override var title: String,
    var items: MutableList<WordItemBundle>,
) : HomeSection()
