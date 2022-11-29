package com.example.spotifyexplained.database

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.spotifyexplained.model.Track

@Entity(tableName = "track_recommend_track_table")
class TrackRecommendEntity(@PrimaryKey override val id: String,
                           @Embedded override val track: Track
) : BaseTrackEntity()