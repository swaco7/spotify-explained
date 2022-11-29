package com.example.spotifyexplained.general

import com.example.spotifyexplained.model.Track

interface GraphClickHandler {
    fun onBasicZoomClick()

    fun onResponsiveZoomClick()

    fun onCloseBundleClick()

    fun onTrackIconClick() {}

    fun onGenreIconClick() {}

    fun onFeatureIconClick() {}

    fun onRelatedIconClick() {}

    fun onInfoIconClick() {}
}