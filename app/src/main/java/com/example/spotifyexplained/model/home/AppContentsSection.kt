package com.example.spotifyexplained.model.home

data class AppContentsSection(
    override var type: HomeSectionType,
    override var title: String,
    var items: MutableList<PageItem>,
) : HomeSection()
