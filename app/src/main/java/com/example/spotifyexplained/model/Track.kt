package com.example.spotifyexplained.model

import androidx.room.Embedded
import com.google.gson.annotations.SerializedName

data class Track(
    @SerializedName("id")
    var trackId: String,

    @SerializedName("name")
    val trackName: String,

    @SerializedName("album")
    @Embedded val album: Album,

    @SerializedName("artists")
    val artists: Array<Artist>,

    @SerializedName("genres")
    var trackGenres: Array<String>?,

    @SerializedName("related_artists")
    var track_related_artists: List<Artist>?,

    @SerializedName("preview_url")
    var preview_url: String? = "",

    @SerializedName("uri")
    var uri: String? = "",

    @SerializedName("popularity")
    var popularity: Int
) {

    override fun hashCode(): Int {
        return (trackName + artists[0].artistName).hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Track
        if (trackName == other.trackName && artists[0].artistName == artists[0].artistName) return true
        if (trackId != other.trackId) return false

        return true
    }
}


