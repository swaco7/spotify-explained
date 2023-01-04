package com.example.spotifyexplained.repository

import androidx.annotation.WorkerThread
import com.example.spotifyexplained.database.TrackDao
import com.example.spotifyexplained.database.entity.*
import kotlinx.coroutines.flow.Flow

class TrackRepository(private val trackDao: TrackDao) {
    val allTrackTracks: Flow<MutableList<TrackRecommendEntity>> = trackDao.getTrackAllTracks()
    val allRecommendedSpecificEntityTracks: Flow<MutableList<TrackSpecificEntity>> = trackDao.getAllTracksSpecific()
    val allArtistTracks: Flow<MutableList<ArtistRecommendEntity>> = trackDao.getArtistAllTracks()
    val allGenreTracks: Flow<MutableList<GenreRecommendEntity>> = trackDao.getGenreAllTracks()
    val allCombinedTracks : Flow<MutableList<CombinedRecommendEntity>> = trackDao.getCombinedAllTracks()
    val allRecommendedTracks: Flow<MutableList<TrackEntity>> = trackDao.getAllTracks()
    val allTracksPool: Flow<MutableList<TrackInPoolEntity>> = trackDao.getAllTracksInPool()
    val allTracksCustom: Flow<MutableList<TrackCustomEntity>> = trackDao.getAllTracksCustom()
    val allUserTracks: Flow<MutableList<UserTrackEntity>> = trackDao.getAllUserTracks()
    val allUserArtists: Flow<MutableList<UserArtistEntity>> = trackDao.getAllUserArtists()
    val allPlaylistTracks: Flow<MutableList<PlaylistTrackEntity>> = trackDao.getPlaylistTracks()
    val allPlaylistNextTrack: Flow<MutableList<PlaylistNextTrackEntity>> = trackDao.getPlaylistNextTrack()
    val allPlaylistFeatures: Flow<MutableList<FeaturesWeightEntity>> = trackDao.getPlaylistFeatures()
    val allRandomTracks: Flow<MutableList<RandomTrackEntity>> = trackDao.getRandomTracks()

    @WorkerThread
    suspend fun insertTracks(tracks: List<TrackRecommendEntity>) {
        trackDao.insertTracks(tracks)
    }

    @WorkerThread
    suspend fun insertArtists(tracks: List<ArtistRecommendEntity>) {
        trackDao.insertArtists(tracks)
    }

    @WorkerThread
    suspend fun insertGenres(tracks: List<GenreRecommendEntity>) {
        trackDao.insertGenres(tracks)
    }

    @WorkerThread
    suspend fun insertCombined(tracks: List<CombinedRecommendEntity>) {
        trackDao.insertCombined(tracks)
    }

    @WorkerThread
    suspend fun insert(track: TrackEntity) {
        trackDao.insert(track)
    }

    @WorkerThread
    suspend fun insert(trackEntity: TrackSpecificEntity) {
        trackDao.insertSpecific(trackEntity)
    }

    @WorkerThread
    suspend fun insertSpecificAll(tracks: List<TrackSpecificEntity>) {
        trackDao.insertSpecificAll(tracks)
    }

    @WorkerThread
    suspend fun insertOverall(tracks: List<TrackEntity>) {
        trackDao.insertOverall(tracks)
    }

    @WorkerThread
    suspend fun insertCustomAll(tracks: List<TrackCustomEntity>) {
        trackDao.insertCustomAll(tracks)
    }

    @WorkerThread
    suspend fun insert(trackEntity: TrackInPoolEntity) {
        trackDao.insertToPool(trackEntity)
    }

    @WorkerThread
    suspend fun insertListToPool(tracks: List<TrackInPoolEntity>) {
        trackDao.insertListToPool(tracks)
    }

    @WorkerThread
    suspend fun insertListToUserArtists(artists: List<UserArtistEntity>) {
        trackDao.insertListToUserArtists(artists)
    }

    @WorkerThread
    suspend fun insertToPlaylist(track: PlaylistTrackEntity) {
        trackDao.insertToPlaylist(track)
    }

    @WorkerThread
    suspend fun insertToRandomTracks(tracks: List<RandomTrackEntity>) {
        trackDao.insertRandomTracks(tracks)
    }

    @WorkerThread
    suspend fun insertNextPlaylist(track: PlaylistNextTrackEntity) {
        trackDao.insertNext(track)
    }

    @WorkerThread
    suspend fun insertFeatures(features: FeaturesWeightEntity) {
        trackDao.insertFeatures(features)
    }

    @WorkerThread
    suspend fun updateFeatures(features: FeaturesWeightEntity) {
        trackDao.updateFeatures(features)
    }

    @WorkerThread
    suspend fun delete() {
        trackDao.deleteAll()
    }

    @WorkerThread
    suspend fun deleteSpecific() {
        trackDao.deleteAllSpecific()
    }

    @WorkerThread
    suspend fun deleteCustom() {
        trackDao.deleteAllCustom()
    }

    @WorkerThread
    suspend fun deleteArtist() {
        trackDao.deleteArtistAll()
    }

    @WorkerThread
    suspend fun deleteTrack() {
        trackDao.deleteTrackAll()
    }

    @WorkerThread
    suspend fun deleteGenre() {
        trackDao.deleteGenreAll()
    }

    @WorkerThread
    suspend fun deleteCombined() {
        trackDao.deleteCombinedAll()
    }

    @WorkerThread
    suspend fun deletePool() {
        trackDao.deleteAllInPool()
    }

    @WorkerThread
    suspend fun deleteUserTracks() {
        trackDao.deleteAllUserTracks()
    }

    @WorkerThread
    suspend fun deleteUserArtists() {
        trackDao.deleteAllUserArtists()
    }

    @WorkerThread
    suspend fun deletePlaylist() {
        trackDao.deletePlaylist()
    }

    @WorkerThread
    suspend fun deleteFromPlaylist(track: PlaylistTrackEntity) {
        trackDao.deleteFromPlaylist(track)
    }

    @WorkerThread
    suspend fun deletePlaylistNext() {
        trackDao.deletePlaylistNext()
    }

    @WorkerThread
    suspend fun deleteFeatures() {
        trackDao.deleteFeatures()
    }

    @WorkerThread
    suspend fun deleteRandomTracks() {
        trackDao.deleteRandomTracks()
    }

    @WorkerThread
    suspend fun deleteAll(){
        delete()
        deleteTrack()
        deleteArtist()
        deleteGenre()
        deleteCombined()
        deletePool()
        deleteSpecific()
        deleteCustom()
        deletePlaylist()
        deletePlaylistNext()
        deleteUserArtists()
        deleteUserTracks()
        deleteFeatures()
        deleteRandomTracks()
    }
}
