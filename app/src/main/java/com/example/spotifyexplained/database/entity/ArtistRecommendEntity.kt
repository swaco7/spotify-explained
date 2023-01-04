package com.example.spotifyexplained.database.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.spotifyexplained.model.Track

@Entity(tableName = "artist_recommend_track_table")
class ArtistRecommendEntity(@PrimaryKey override val id: String,
                            @Embedded override val track: Track
) : BaseTrackEntity()