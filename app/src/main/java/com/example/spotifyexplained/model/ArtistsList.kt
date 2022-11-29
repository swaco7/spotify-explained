package com.example.spotifyexplained.model

import com.google.gson.annotations.SerializedName

data class ArtistsList(
    @SerializedName("artists")
    val artists: Array<Artist>
    )
