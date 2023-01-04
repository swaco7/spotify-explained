package com.example.spotifyexplained.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.spotifyexplained.database.entity.*

@Database(
    entities = [
        TrackEntity::class,
        TrackSpecificEntity::class,
        TrackCustomEntity::class,
        TrackInPoolEntity::class,
        TrackRecommendEntity::class,
        ArtistRecommendEntity::class,
        GenreRecommendEntity::class,
        CombinedRecommendEntity::class,
        UserTrackEntity::class,
        UserArtistEntity::class,
        PlaylistTrackEntity::class,
        PlaylistDiscardedTrackEntity::class,
        PlaylistNextTrackEntity::class,
        FeaturesWeightEntity::class,
        RandomTrackEntity::class
    ], version = 13, exportSchema = false
)
@TypeConverters(Converters::class)
abstract class TrackRoomDatabase : RoomDatabase() {

    abstract fun trackDao(): TrackDao

    companion object {
        @Volatile
        private var INSTANCE: TrackRoomDatabase? = null

        fun getDatabase(context: Context): TrackRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TrackRoomDatabase::class.java,
                    "track_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }

}