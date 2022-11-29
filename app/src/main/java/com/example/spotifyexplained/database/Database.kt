package com.example.spotifyexplained.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.spotifyexplained.dao.TrackDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

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
        FeaturesWeightEntity::class
    ], version = 11, exportSchema = false
)
@TypeConverters(Converters::class)
abstract class TrackRoomDatabase : RoomDatabase() {

    abstract fun trackDao(): TrackDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: TrackRoomDatabase? = null

        fun getDatabase(context: Context): TrackRoomDatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TrackRoomDatabase::class.java,
                    "track_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }

    private class TrackDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    populateDatabase(database.trackDao())
                }
            }
        }

        suspend fun populateDatabase(trackDao: TrackDao) {
            // Delete all content here.
            trackDao.deleteAll()
        }
    }

}