package com.example.spotifyexplained.general

import com.example.spotifyexplained.model.Track

interface TrackDetailClickHandler {
    fun onTrackClick(track: Track?) {}

    fun onTrackClick(trackName: String?){
        onTrackClick(trackName)
    }

    fun onSimilarTrackClick(trackId: String?){}

    fun onTrackLongClick(track: Track?) {
        onTrackClick(track)
    }
}