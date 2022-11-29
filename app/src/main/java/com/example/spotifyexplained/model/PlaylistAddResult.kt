package com.example.spotifyexplained.model

import androidx.room.Embedded
import com.google.gson.annotations.SerializedName

data class PlaylistAddResult(
    @SerializedName("snapshot_id")
    val snapshot_id: String,
)
