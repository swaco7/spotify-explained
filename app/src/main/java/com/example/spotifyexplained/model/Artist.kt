package com.example.spotifyexplained.model

import com.google.gson.annotations.SerializedName

data class Artist(
    @SerializedName("id")
    val artistId: String,

    @SerializedName("name")
    val artistName:String,

    @SerializedName("popularity")
    var artistPopularity: Int?,

    @SerializedName("genres")
    var genres: Array<String>?,

    @SerializedName("images")
    var images: Array<Image>?,

    @SerializedName("related_artists")
    var related_artists: List<Artist>

) {
    override fun hashCode(): Int {
        return artistId.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Artist
        if (artistId != other.artistId) return false
        return true
    }
}

