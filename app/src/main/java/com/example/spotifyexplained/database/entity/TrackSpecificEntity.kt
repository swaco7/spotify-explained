package com.example.spotifyexplained.database.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.spotifyexplained.model.AudioFeatures
import com.example.spotifyexplained.model.Track

@Entity(tableName = "recommend_track_specific_table")
class TrackSpecificEntity(@PrimaryKey override val id: String,
                          @Embedded override val track: Track,
                          @Embedded val features: AudioFeatures
) : BaseTrackEntity()
