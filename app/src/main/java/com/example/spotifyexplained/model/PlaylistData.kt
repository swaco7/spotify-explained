package com.example.spotifyexplained.model

import androidx.room.Embedded
import com.google.gson.annotations.SerializedName

data class PlaylistData(
    @SerializedName("name")
    val name: String,
    @SerializedName("public")
    val public: Boolean,
    @SerializedName("collaborative")
    val collaborative: Boolean,
    @SerializedName("description")
    val description: String
)
