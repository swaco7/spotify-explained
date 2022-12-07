package com.example.spotifyexplained.database

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.spotifyexplained.model.AudioFeatures
import com.example.spotifyexplained.model.Track

@Entity(tableName = "random_tracks_table")
class RandomTrackEntity(@PrimaryKey override val id: String,
                        @Embedded override val track: Track,
                        @Embedded val features: AudioFeatures,
) : BaseTrackEntity()
