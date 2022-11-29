package com.example.spotifyexplained.database

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.spotifyexplained.model.AudioFeatures
import com.example.spotifyexplained.model.Track

@Entity(tableName = "feature_weight_table")
class FeaturesWeightEntity(@PrimaryKey val id: String,
                           @Embedded val audioFeatures: AudioFeatures
)