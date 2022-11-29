package com.example.spotifyexplained.model.home

data class HomeStatsSection(
    override var type: HomeSectionType,
    override var title: String,
    var items: MutableList<StatsSection>
) : HomeSection()
