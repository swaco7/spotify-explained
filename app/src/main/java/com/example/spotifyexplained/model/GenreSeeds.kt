package com.example.spotifyexplained.model

import androidx.room.Embedded
import com.google.gson.annotations.SerializedName

data class GenreSeeds(
    @SerializedName("genres")
    val seeds: Array<String>
)
