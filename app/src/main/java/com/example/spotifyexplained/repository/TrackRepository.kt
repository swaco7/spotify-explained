package com.example.spotifyexplained.repository

import androidx.annotation.WorkerThread
import com.example.spotifyexplained.dao.TrackDao
import com.example.spotifyexplained.database.*
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
    val allPlaylistDiscardedTracks: Flow<MutableList<PlaylistDiscardedTrackEntity>> = trackDao.getPlaylistDiscardedTracks()
    val allPlaylistNextTrack: Flow<MutableList<PlaylistNextTrackEntity>> = trackDao.getPlaylistNextTrack()
    val allPlaylistFeatures: Flow<MutableList<FeaturesWeightEntity>> = trackDao.getPlaylistFeatures()

    @WorkerThread
    suspend fun insertTrack(track: TrackRecommendEntity) {
        trackDao.insertTrack(track)
    }

    @WorkerThread
    suspend fun insertTracks(tracks: List<TrackRecommendEntity>) {
        trackDao.insertTracks(tracks)
    }

    @WorkerThread
    suspend fun insertArtist(track: ArtistRecommendEntity) {
        trackDao.insertArtist(track)
    }

    @WorkerThread
    suspend fun insertArtists(tracks: List<ArtistRecommendEntity>) {
        trackDao.insertArtists(tracks)
    }

    @WorkerThread
    suspend fun insertGenre(track: GenreRecommendEntity) {
        trackDao.insertGenre(track)
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

//    @WorkerThread
//    suspend fun insert(trackEntity: TrackCustomEntity) {
//        trackDao.insertCustom(trackEntity)
//    }

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
    suspend fun insertListToUserTracks(tracks: List<UserTrackEntity>) {
        trackDao.insertListToUserTracks(tracks)
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
    suspend fun insertToDiscardedPlaylist(track: PlaylistDiscardedTrackEntity) {
        trackDao.insertToDiscardedPlaylist(track)
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
    suspend fun deletePlaylistDiscarded() {
        trackDao.deletePlaylistDiscarded()
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


}
