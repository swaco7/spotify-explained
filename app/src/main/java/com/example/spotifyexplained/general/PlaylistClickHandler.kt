package com.example.spotifyexplained.general

import com.example.spotifyexplained.model.Track

interface PlaylistClickHandler {
    fun onLikeClick()

    fun onNeutralClick()

    fun onDislikeClick()

}