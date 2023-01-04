package com.example.spotifyexplained.database.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.spotifyexplained.model.Track

@Entity
abstract class BaseTrackEntity {
    abstract val id: String
    abstract val track: Track
}
