package com.example.spotifyexplained.ui.home

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.example.spotifyexplained.R
import com.example.spotifyexplained.activity.MainActivity
import com.example.spotifyexplained.database.UserArtistEntity
import com.example.spotifyexplained.model.*
import com.example.spotifyexplained.model.home.*
import com.example.spotifyexplained.repository.TrackRepository
import com.example.spotifyexplained.services.ApiHelper
import com.example.spotifyexplained.services.SessionManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.min
import kotlin.random.Random

class HomeViewModel(activity: Activity, private val repository: TrackRepository) : ViewModel() {
    val loadingState = MutableLiveData<LoadingState>().apply {
        value = LoadingState.LOADING
    }

    private val topGenres: MutableLiveData<MutableList<StatsSectionItem>> by lazy {
        MutableLiveData<MutableList<StatsSectionItem>>(mutableListOf())
    }

    private val userTracks: MutableLiveData<MutableList<Track>> by lazy {
        MutableLiveData<MutableList<Track>>(mutableListOf())
    }

    private val userTracksWithFeatures: MutableLiveData<MutableList<TrackAudioFeatures>> by lazy {
        MutableLiveData<MutableList<TrackAudioFeatures>>(mutableListOf())
    }

    private val topFeatures: MutableLiveData<MutableList<StatsSectionItem>> by lazy {
        MutableLiveData<MutableList<StatsSectionItem>>(mutableListOf())
    }

    private val mostPopularTracks: MutableLiveData<MutableList<StatsSectionItem>> by lazy {
        MutableLiveData<MutableList<StatsSectionItem>>(mutableListOf())
    }

    private val leastPopularTracks: MutableLiveData<MutableList<StatsSectionItem>> by lazy {
        MutableLiveData<MutableList<StatsSectionItem>>(mutableListOf())
    }

    private val mostPopularArtists: MutableLiveData<MutableList<StatsSectionItem>> by lazy {
        MutableLiveData<MutableList<StatsSectionItem>>(mutableListOf())
    }

    private val leastPopularArtists: MutableLiveData<MutableList<StatsSectionItem>> by lazy {
        MutableLiveData<MutableList<StatsSectionItem>>(mutableListOf())
    }

    private val generalSetTracksWithFeatures: MutableLiveData<MutableList<TrackAudioFeatures>> by lazy {
        MutableLiveData<MutableList<TrackAudioFeatures>>(mutableListOf())
    }

    val statsItems: MutableLiveData<MutableList<HomeSection>> by lazy {
        MutableLiveData<MutableList<HomeSection>>(mutableListOf())
    }

    val state = MutableLiveData<VisualState>().apply {
        value = VisualState.TABLE
    }

    val userName = MutableLiveData<String>().apply {
        value = SessionManager.getUserId()
    }

    private val fullUserArtists: Flow<MutableList<UserArtistEntity>> = repository.allUserArtists

//    private fun insertUsersArtists(artists: MutableList<Artist>) = viewModelScope.launch {
//        repository.insertListToUserArtists(artists.map { UserArtistEntity(
//            it.artistId,
//            it,
//            TrackAudioFeatures(Track("mock", "mock", Album("mock", arrayOf(), "mock"), arrayOf(), null, null, null, null, 0), AudioFeatures("")),
//            true
//        ) })
//    }

    private fun insertUserItems(tracks: MutableList<TrackAudioFeatures>, artists: MutableList<Artist>){
        val items = mutableListOf<UserArtistEntity>()
        items.addAll(artists.map { UserArtistEntity(
            it.artistId,
            it,
            TrackAudioFeatures(Track("mock", "mock", Album("mock", arrayOf(), "mock"), arrayOf(), null, null, null, null, 0), AudioFeatures("")),
            true
        ) })
        items.addAll(tracks.map { UserArtistEntity(it.track.trackId, it.track.artists[0], it, false) })
        viewModelScope.launch {
            repository.insertListToUserArtists(items)
        }
    }

//    private fun insertUsersTracksArtists(tracks: MutableList<TrackAudioFeatures>) = viewModelScope.launch {
//        repository.insertListToUserArtists(tracks.map { UserArtistEntity(it.track.artists[0].artistId, it.track.artists[0], it, false) })
//    }

    private val topArtists: MutableLiveData<MutableList<Artist>> by lazy {
        MutableLiveData<MutableList<Artist>>(mutableListOf())
    }

    private val topTracksArtists: MutableLiveData<MutableList<Artist>> by lazy {
        MutableLiveData<MutableList<Artist>>(mutableListOf())
    }

    private val context: Context? by lazy {
        activity
    }

    private val random = Random(42)

    init {
        if (SessionManager.tokenExpired()){
            (activity as MainActivity).authorizeUser()
        }  else {
            viewModelScope.launch {
                loadingState.value = LoadingState.LOADING
                fullUserArtists.collect { artists ->
                    if (SessionManager.tokenExpired()) {
                        (activity as MainActivity).authorizeUser()
                    } else {
                        if (artists.isEmpty()) {
                            Log.e("prepare", "--- Download top artists ---")
                            getUserTopArtists()
                            getUsersArtistsRelatedArtist()
                            getUserTopTracks()
                            getTracksRelatedArtists()
                        } else {
                            Log.e("prepare", "--- Top artists downloaded ---")
                            topArtists.value = artists.filter { it.isUserArtist }.map { it.artist }.toMutableList()
                            userTracksWithFeatures.value = artists.filter { !it.isUserArtist }.map { it.track!! }.toMutableList()
                            (context as MainActivity).viewModel.topArtists.value = topArtists.value
                            (context as MainActivity).viewModel.topTracks.value = userTracksWithFeatures.value
                            //loadingState.value = LoadingState.SETTINGS_LOADED
                            Log.e("prepare", "--- Top artists loaded ---")
                        }
                        getGeneralSetTracks()
                        getTopGenresSeeds()
                        getUserTopTracks()
                        getAudioFeatures()
                        getMostPopularTracks()
                        getLeastPopularTracks()
                        getMostPopularArtists()
                        getLeastPopularArtists()
                        prepareSections()
                        loadingState.value = LoadingState.SUCCESS
                    }
                }
            }
        }
    }

    /**
     * Clear all database tables
     */
    fun clearCaches(){
        viewModelScope.launch {
            repository.delete()
            repository.deleteTrack()
            repository.deleteArtist()
            repository.deleteGenre()
            repository.deleteCombined()
            repository.deletePool()
            repository.deleteSpecific()
            repository.deleteCustom()
            repository.deletePlaylist()
            repository.deletePlaylistNext()
            repository.deleteUserArtists()
            repository.deleteUserTracks()
            repository.deleteFeatures()
        }
    }

    /**
     * Gather most incident genre seeds in user's top artist
     */
    private fun getTopGenresSeeds(){
        val genreMap = HashMap<String,Int>()
        for (artist in topArtists.value!!){
            for (genre in artist.genres!!) {
                genreMap[genre] = if (genreMap.containsKey(genre)) genreMap.getValue(genre) + 1 else 1
            }
        }
        val sortedGenres = ArrayList<StatsSectionItem>()
        genreMap.entries.sortedByDescending{it.value}.forEach{sortedGenres.add(StatsSectionItem(name = it.key, value = (it.value.toDouble() / topArtists.value!!.size)*100))}
        topGenres.value = sortedGenres
    }

    /**
     * Gather mean values of audio features mapped to interval [0,1]
     */
    private suspend fun getAudioFeatures() {
        val featuresSums = ArrayList<StatsSectionItem>()
        for (index in AudioFeatureType.values().indices) {
            val usersTracksValue = calculateDifference(
                AudioFeatureType.values()[index],
                userTracksWithFeatures.value!!.sumOf { it.features.at(index)!! } / userTracksWithFeatures.value!!.size
            ) * 100
            val generalTracksValue = calculateDifference(
                AudioFeatureType.values()[index],
                generalSetTracksWithFeatures.value!!.sumOf { it.features.at(index)!! } / generalSetTracksWithFeatures.value!!.size
            ) * 100
            featuresSums.add(StatsSectionItem(
                name = AudioFeatureType.values()[index].name,
                value = usersTracksValue - generalTracksValue
            ))
        }
        topFeatures.value = featuresSums.sortedByDescending { it.value }.toMutableList()
    }

    /**
     * Maps value to interval [0,1]
     * @param [featureType] audio feature
     * @param [absoluteMean] mean value of audio feature
     * @return mapped mean value
     */
    private fun calculateDifference(featureType: AudioFeatureType, absoluteMean: Double): Double{
        return when (featureType) {
            AudioFeatureType.LOUDNESS -> ((absoluteMean - (-60)) / (0 - (-60)))
            AudioFeatureType.TEMPO -> (absoluteMean - 0) / (190 - 0)
            else -> absoluteMean
        }
    }

    /**
     * Returns tracks from user's tracks by popularity in the descending order
     */
    private fun getMostPopularTracks(){
        mostPopularTracks.value = userTracksWithFeatures.value!!.sortedByDescending { it.track.popularity }.map {
            StatsSectionItem(trackId = it.track.trackId, name = it.track.trackName, value = it.track.popularity.toDouble(), artistName = it.track.artists[0].artistName, imageUrl = it.track.album.albumImages[0].url)
        }.toMutableList()
    }

    /**
     * Returns tracks from user's tracks by popularity in the ascending order
     */
    private fun getLeastPopularTracks() {
        leastPopularTracks.value = userTracksWithFeatures.value!!.sortedBy { it.track.popularity }.map {
            StatsSectionItem(trackId = it.track.trackId, name = it.track.trackName, value = it.track.popularity.toDouble(), artistName = it.track.artists[0].artistName, imageUrl = it.track.album.albumImages[0].url)
        }.filter { it.value > 0 }.toMutableList()
    }

    /**
     * Returns tracks from user's tracks by popularity in the descending order
     */
    private fun getMostPopularArtists(){
        mostPopularArtists.value = topArtists.value!!.sortedByDescending { it.artistPopularity }.map {
            StatsSectionItem(name = it.artistName, value = it.artistPopularity!!.toDouble(), imageUrl = it.images!![0].url)
        }.toMutableList()
    }

    /**
     * Returns tracks from user's tracks by popularity in the ascending order
     */
    private fun getLeastPopularArtists() {
        leastPopularArtists.value = topArtists.value!!.sortedBy { it.artistPopularity }.map {
            StatsSectionItem(name = it.artistName, value = it.artistPopularity!!.toDouble(), imageUrl = it.images!![0].url)
        }.filter { it.value > 0 }.toMutableList()
    }

    private suspend fun getGeneralSetTracks(){
        val generalTracks = mutableListOf<Track>()
        val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        for (index in 0 until 10){
            val randomChar = random.nextInt(0, charPool.size)
            val offset = random.nextInt(0, 100)
            generalTracks.addAll(ApiHelper.getSearchTracks("%${charPool[randomChar]}%",  limit = 2, offset = offset, "track") ?: mutableListOf())
        }
        val generalWithFeatures = ApiHelper.getTracksAudioFeatures(generalTracks)
        generalSetTracksWithFeatures.value = generalWithFeatures?.toMutableList()
    }

    /**
     * Prepares homepage sections
     */
    private fun prepareSections() {
        val pagesList = mutableListOf<PageItem>()
        pagesList.add(PageItem("Our recommendations", context!!.getString(R.string.home_our_recommend_content), PageType.RECOMMEND))
        pagesList.add(PageItem("Your music", context!!.getString(R.string.home_your_music_content), PageType.USERTOP))
        pagesList.add(PageItem("Spotify recommend", context!!.getString(R.string.home_spotify_recommend_content), PageType.SPOTIFY))
        pagesList.add(PageItem("Playlist creation", context!!.getString(R.string.home_playlist_creation), PageType.PLAYLIST))

        val wordBundles = mutableListOf<WordItemBundle>()
        wordBundles.add(WordItemBundle("Top genres", topGenres.value!!.map { WordItem(it.name, (it.value*1.25).toInt() + 8) }.toMutableList()))
        wordBundles.add(WordItemBundle("Top artists", topArtists.value!!.map { WordItem(it.artistName, (it.artistPopularity!!*0.2).toInt() + 2) }.toMutableList()))

        val statsSectionList = mutableListOf<StatsSection>()
        statsSectionList.add(StatsSection(StatsSectionType.MOSTPOP, mostPopularTracks.value!!, "Tracks", "Popularity of track calculated by Spotify"))
        statsSectionList.add(StatsSection(StatsSectionType.MOSTPOP, mostPopularArtists.value!!, "Artists", "Popularity of artist, calculated from popularity of artist's tracks by Spotify"))

        val statsSectionList2 = mutableListOf<StatsSection>()
        statsSectionList2.add(StatsSection(StatsSectionType.LEASTPOP, leastPopularTracks.value!!, "Tracks", "Popularity of track calculated by Spotify"))
        statsSectionList2.add(StatsSection(StatsSectionType.LEASTPOP, leastPopularArtists.value!!, "Artists", "Popularity of artist, calculated from popularity of artist's tracks by Spotify"))

        val statsSectionList3 = mutableListOf<StatsSection>()
        statsSectionList3.add(StatsSection(StatsSectionType.GENRE, topGenres.value!!, "Genres", "Occurrence of genre in user's music"))
        statsSectionList3.add(StatsSection(StatsSectionType.FEATURES, topFeatures.value!!, "Audio features", "Average value of audio feature from user's music relative to average value from \"random\" set of tracks"))

        val homeSectionList = mutableListOf<HomeSection>()
        homeSectionList.add(AppContentsSection(HomeSectionType.APPCONTENTS, "App content", pagesList))
        homeSectionList.add(WordCloudsSection(HomeSectionType.WORDCLOUD, "WordCloud", wordBundles))
        homeSectionList.add(HomeStatsSection(HomeSectionType.STATS, "\"Best-sellers\" you like", statsSectionList))
        homeSectionList.add(HomeStatsSection(HomeSectionType.STATS, "Your hidden gems", statsSectionList2))
        homeSectionList.add(HomeStatsSection(HomeSectionType.STATS, "Top", statsSectionList3))

        statsItems.value = homeSectionList
    }

    /**
     * Gathers user's top tracks from Spotify topTracks and savedTracks
     */
    private suspend fun getUserTopTracks(){
        val topTracksList = ApiHelper.getUserTopTracks(Constants.topItemsLimit) ?: mutableListOf()
        val sizeDifference = Constants.topItemsLimit - topTracksList.size
        if (sizeDifference > 0) {
            val savedTracks = ApiHelper.getUserSavedTracks(Constants.savedTracksLimit) ?: mutableListOf()
            val savedTracksNew = savedTracks.filter { !topTracksList.contains(it) }
            val savedTracksFinal = savedTracksNew.take(min(sizeDifference, savedTracksNew.size))
            topTracksList.addAll(savedTracksFinal)
        }
        userTracks.value = topTracksList.toMutableList()
        userTracksWithFeatures.value = ApiHelper.getTracksAudioFeatures(userTracks.value!!)?.toMutableList()
    }

    /**
     * Gathers user's top artists from Spotify topArtists and savedTracks
     */
    private suspend fun getUserTopArtists() {
        val topArtistsList = ApiHelper.getUserTopArtists(Constants.topItemsLimit) ?: mutableListOf()
        val sizeDifference = Constants.topItemsLimit - topArtistsList.size
        if (sizeDifference > 0) {
            val savedTracks = ApiHelper.getUserSavedTracks(Constants.savedTracksLimit) ?: mutableListOf()
            val savedArtists = savedTracks.map { it.artists[0] }.filter { !topArtistsList.contains(it) }
            val frequencyMap = mutableMapOf<Artist, Int>()
            for (artist in savedArtists.distinct()) {
                frequencyMap[artist] = Collections.frequency(savedArtists, artist)
            }
            val sortedArtists = frequencyMap.toList().sortedBy { (_, value) -> value }.map { it.first }
            val sortedArtistsFinal = ApiHelper.getArtistsDetail(
                sortedArtists.take(
                    min(
                        sizeDifference,
                        sortedArtists.size
                    )
                ).toMutableList()
            )
            topArtistsList.addAll(sortedArtistsFinal?.artists?.toMutableList() ?: mutableListOf())
        }
        topArtists.value = topArtistsList
    }

    /**
     * Gathers related artist for user's top artists
     */
    private suspend fun getUsersArtistsRelatedArtist() {
        for (artist in topArtists.value!!) {
            val relatedArtists = ApiHelper.getRelatedArtists(artist.artistId)
            topArtists.value!![topArtists.value!!.indexOf(artist)].related_artists =
                relatedArtists?.toList() ?: mutableListOf()
        }
        Log.e("prepare", "--- InsertTopArtists ---")
        //insertUsersArtists(topArtists.value!!)
    }

    /**
     * Assigns related artist to each track's artist
     */
    private suspend fun getTracksRelatedArtists() {
        for (track in userTracksWithFeatures.value!!) {
            var trackArtist = (context as MainActivity).viewModel.topArtists.value!!.firstOrNull { it.artistId == track.track.artists[0].artistId }
            if (trackArtist == null) {
                trackArtist = userTracksWithFeatures.value!!.firstOrNull { it.track.artists[0].artistId == track.track.artists[0].artistId }?.track?.artists?.get(0)
                if (trackArtist?.related_artists != null){
                    Log.e("request", "success")
                }
            } else {
                Log.e("request", "successArtist")
            }
            track.track.artists[0].related_artists = trackArtist?.related_artists
                ?: (ApiHelper.getRelatedArtists(track.track.artists[0].artistId)?.toList() ?: mutableListOf())

        }
        val response =
            ApiHelper.getArtistWithGenres(userTracksWithFeatures.value!!.map { it.track }.toMutableList()) ?: return
        for (i in response.artists.indices) {
            userTracksWithFeatures.value!![i].track.trackGenres = response.artists[i].genres
            userTracksWithFeatures.value!![i].track.artists[0].genres = response.artists[i].genres
            userTracksWithFeatures.value!![i].track.artists[0].artistPopularity =
                response.artists[i].artistPopularity
            userTracksWithFeatures.value!![i].track.artists[0].images = response.artists[i].images
        }
        Log.e("prepare", "--- InsertToptracks ---")
        insertUserItems(userTracksWithFeatures.value!!, topArtists.value!!)
    }
}