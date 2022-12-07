package com.example.spotifyexplained.database

import androidx.room.*
import com.example.spotifyexplained.database.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {
    @Query("SELECT * FROM genre_recommend_track_table")
    fun getGenreAllTracks(): Flow<MutableList<GenreRecommendEntity>>

    @Query("SELECT * FROM artist_recommend_track_table")
    fun getArtistAllTracks(): Flow<MutableList<ArtistRecommendEntity>>

    @Query("SELECT * FROM track_recommend_track_table")
    fun getTrackAllTracks(): Flow<MutableList<TrackRecommendEntity>>

    @Query("SELECT * FROM recommend_track_table")
    fun getAllTracks(): Flow<MutableList<TrackEntity>>

    @Query("SELECT * FROM recommend_track_specific_table")
    fun getAllTracksSpecific(): Flow<MutableList<TrackSpecificEntity>>

    @Query("SELECT * FROM recommend_track_custom_table")
    fun getAllTracksCustom(): Flow<MutableList<TrackCustomEntity>>

    @Query("SELECT * FROM recommend_track_pool_table")
    fun getAllTracksInPool(): Flow<MutableList<TrackInPoolEntity>>

    @Query("SELECT * FROM combined_recommend_track_table")
    fun getCombinedAllTracks(): Flow<MutableList<CombinedRecommendEntity>>

    @Query("SELECT * FROM user_track_table")
    fun getAllUserTracks(): Flow<MutableList<UserTrackEntity>>

    @Query("SELECT * FROM user_artist_table")
    fun getAllUserArtists(): Flow<MutableList<UserArtistEntity>>

    @Query("SELECT * FROM playlist_track_table")
    fun getPlaylistTracks(): Flow<MutableList<PlaylistTrackEntity>>

    @Query("SELECT * FROM playlist_discarded_track_table")
    fun getPlaylistDiscardedTracks(): Flow<MutableList<PlaylistDiscardedTrackEntity>>

    @Query("SELECT * FROM playlist_next_track_table")
    fun getPlaylistNextTrack(): Flow<MutableList<PlaylistNextTrackEntity>>

    @Query("SELECT * FROM feature_weight_table")
    fun getPlaylistFeatures(): Flow<MutableList<FeaturesWeightEntity>>

    @Query("SELECT * FROM random_tracks_table")
    fun getRandomTracks(): Flow<MutableList<RandomTrackEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTrack(track: TrackRecommendEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTracks(track: List<TrackRecommendEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertArtist(track: ArtistRecommendEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtists(tracks: List<ArtistRecommendEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertGenre(track: GenreRecommendEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGenres(tracks: List<GenreRecommendEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCombined(tracks: List<CombinedRecommendEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(track: TrackEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOverall(tracks: List<TrackEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSpecific(trackEntity: TrackSpecificEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSpecificAll(tracks: List<TrackSpecificEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCustom(trackEntity: TrackCustomEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCustomAll(trackEntity: List<TrackCustomEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertToPool(trackEntity: TrackInPoolEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertListToPool(tracks: List<TrackInPoolEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertListToUserTracks(tracks: List<UserTrackEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertListToUserArtists(tracks: List<UserArtistEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertToPlaylist(track: PlaylistTrackEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertToDiscardedPlaylist(track: PlaylistDiscardedTrackEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNext(track: PlaylistNextTrackEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertFeatures(features: FeaturesWeightEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRandomTracks(tracks: List<RandomTrackEntity>)

    @Update
    suspend fun updateFeatures(features: FeaturesWeightEntity)

    @Query("DELETE FROM track_recommend_track_table")
    suspend fun deleteTrackAll()

    @Query("DELETE FROM artist_recommend_track_table")
    suspend fun deleteArtistAll()

    @Query("DELETE FROM genre_recommend_track_table")
    suspend fun deleteGenreAll()

    @Query("DELETE FROM combined_recommend_track_table")
    suspend fun deleteCombinedAll()

    @Query("DELETE FROM recommend_track_table")
    suspend fun deleteAll()

    @Query("DELETE FROM recommend_track_specific_table")
    suspend fun deleteAllSpecific()

    @Query("DELETE FROM recommend_track_custom_table")
    suspend fun deleteAllCustom()

    @Query("DELETE FROM recommend_track_pool_table")
    suspend fun deleteAllInPool()

    @Query("DELETE FROM user_track_table")
    suspend fun deleteAllUserTracks()

    @Query("DELETE FROM user_artist_table")
    suspend fun deleteAllUserArtists()

    @Query("DELETE FROM playlist_track_table")
    suspend fun deletePlaylist()

    @Query("DELETE FROM playlist_discarded_track_table")
    suspend fun deletePlaylistDiscarded()

    @Delete
    suspend fun deleteFromPlaylist(track: PlaylistTrackEntity)

    @Query("DELETE FROM playlist_next_track_table")
    suspend fun deletePlaylistNext()

    @Query("DELETE FROM feature_weight_table")
    suspend fun deleteFeatures()

    @Query("DELETE FROM random_tracks_table")
    suspend fun deleteRandomTracks()
}