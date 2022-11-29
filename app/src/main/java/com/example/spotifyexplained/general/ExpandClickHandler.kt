package com.example.spotifyexplained.general

import com.example.spotifyexplained.model.Track

interface ExpandClickHandler {
    fun onExpandClick(expanded: Boolean)

    fun onTabExpandClick(expanded: Boolean)
}