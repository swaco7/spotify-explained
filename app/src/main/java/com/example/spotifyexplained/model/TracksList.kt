package com.example.spotifyexplained.model

import com.google.gson.annotations.SerializedName

data class TracksList(
    @SerializedName("tracks")
    val tracks: Array<Track>
    )
