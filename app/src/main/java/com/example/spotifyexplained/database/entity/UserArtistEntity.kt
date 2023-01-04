package com.example.spotifyexplained.database.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.spotifyexplained.model.Artist
import com.example.spotifyexplained.model.AudioFeatures
import com.example.spotifyexplained.model.Track
import com.example.spotifyexplained.model.TrackAudioFeatures

@Entity(tableName = "user_artist_table")
class UserArtistEntity(@PrimaryKey val id: String,
                       @Embedded val artist: Artist,
                       @Embedded val track: TrackAudioFeatures?,
                       val isUserArtist: Boolean,
)
