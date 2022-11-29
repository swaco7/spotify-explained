package com.example.spotifyexplained.model

import com.google.gson.annotations.SerializedName

data class RecommendedTracks(
    @SerializedName("seeds")
    val seeds: Array<Seed>,

    @SerializedName("tracks")
    val tracks: Array<Track>
)
