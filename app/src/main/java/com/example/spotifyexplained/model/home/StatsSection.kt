package com.example.spotifyexplained.model.home

class StatsSection (
    val type: StatsSectionType,
    val items: MutableList<StatsSectionItem>,
    val title: String,
    var help: String,
)
