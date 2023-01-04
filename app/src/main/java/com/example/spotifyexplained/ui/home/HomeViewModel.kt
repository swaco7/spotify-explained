package com.example.spotifyexplained.ui.home

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spotifyexplained.R
import com.example.spotifyexplained.activity.MainActivity
import com.example.spotifyexplained.database.entity.RandomTrackEntity
import com.example.spotifyexplained.database.entity.UserArtistEntity
import com.example.spotifyexplained.general.Config
import com.example.spotifyexplained.general.Helper
import com.example.spotifyexplained.model.*
import com.example.spotifyexplained.model.enums.AudioFeatureType
import com.example.spotifyexplained.model.enums.LoadingState
import com.example.spotifyexplained.model.enums.VisualState
import com.example.spotifyexplained.model.home.*
import com.example.spotifyexplained.repository.TrackRepository
import com.example.spotifyexplained.services.ApiRepository
import com.example.spotifyexplained.services.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.max
import kotlin.math.min

class HomeViewModel(activity: Activity, private val repository: TrackRepository) : ViewModel() {
    val loadingState = MutableLiveData<LoadingState>().apply {
        value = LoadingState.LOADING
    }
    val statsItems: MutableLiveData<MutableList<HomeSection>> by lazy {
        MutableLiveData<MutableList<HomeSection>>(mutableListOf())
    }
    val state = MutableLiveData<VisualState>().apply {
        value = VisualState.TABLE
    }
    val userName = MutableLiveData<String>().apply {
        value = ""
    }

    private lateinit var topGenres : List<StatsSectionItem>
    private lateinit var topTracksWithFeatures: List<TrackAudioFeatures>
    private lateinit var topArtists: List<Artist>
    private var generalSetTracksWithFeatures: List<TrackAudioFeatures> = listOf()

    private val fullUserArtists: Flow<MutableList<UserArtistEntity>> = repository.allUserArtists
    private val fullRandomTracks: Flow<MutableList<RandomTrackEntity>> = repository.allRandomTracks

    private fun insertUserItems(tracks: List<TrackAudioFeatures>, artists: List<Artist>){
        val items = mutableListOf<UserArtistEntity>()
        items.addAll(artists.map { UserArtistEntity(
            it.artistId,
            it,
            null,
            true
        ) })
        items.addAll(tracks.map { UserArtistEntity(it.track.trackId, it.track.artists[0], it, false) })
        viewModelScope.launch {
            repository.insertListToUserArtists(items)
        }
    }
    private fun insertRandomTracks(tracks: MutableList<TrackAudioFeatures>){
        viewModelScope.launch {
            repository.insertToRandomTracks(tracks.map { RandomTrackEntity(it.track.trackId, it.track, it.features) })
        }
    }

    private val context: Context? by lazy {
        activity
    }
    init {
        viewModelScope.launch {
            fullUserArtists.collect { artists ->
                loadingState.value = LoadingState.LOADING
                if (artists.isEmpty()) {
                    getUserTopArtists()
                    getUsersArtistsRelatedArtist()
                    getUserTopTracks()
                    getTracksRelatedArtists()
                } else {
                    topArtists = artists.filter { it.isUserArtist }.map { it.artist }
                    topTracksWithFeatures = artists.filter { !it.isUserArtist }.map { it.track!! }
                    (context as MainActivity).viewModel.topArtists.value = topArtists
                    (context as MainActivity).viewModel.topTracks.value = topTracksWithFeatures
                }

                getGeneralSet()
                topGenres = getTopGenresSeeds()
                prepareSections()
                userName.value = getUserName()
                loadingState.value = LoadingState.SUCCESS
            }
        }
    }

    private suspend fun getGeneralSet(){
        viewModelScope.launch {
            fullRandomTracks.collect { randomTracks ->
                if (randomTracks.isNotEmpty()) {
                    generalSetTracksWithFeatures =
                        randomTracks.map { TrackAudioFeatures(it.track, it.features) }
                            .toMutableList()
                    prepareSections()
                } else {
                    getGeneralSetTracks()
                    prepareSections()
                }
            }
        }
    }

    /**
     * Clear all database tables
     */
    fun clearCaches(){
        viewModelScope.launch {
            repository.deleteAll()
        }
    }

    private suspend fun getUserName(): String {
        return ApiRepository.getProfile(context!!)?.display_name ?: ""
    }

    /**
     * Gather most incident genre seeds in user's top artist
     */
    private fun getTopGenresSeeds(): kotlin.collections.ArrayList<StatsSectionItem>{
        val genreMap = HashMap<String,Int>()
        for (artist in topArtists){
            for (genre in artist.genres ?: arrayOf()) {
                genreMap[genre] = if (genreMap.containsKey(genre)) genreMap.getValue(genre) + 1 else 1
            }
        }
        val sortedGenres = ArrayList<StatsSectionItem>()
        genreMap.entries.sortedByDescending{it.value}.forEach{sortedGenres.add(StatsSectionItem(name = it.key, value = (it.value.toDouble() / topArtists.size)*100))}
        return sortedGenres
    }

    /**
     * Gather mean values of audio features mapped to interval [0,1]
     */
    private fun getTopAudioFeatures() : List<StatsSectionItem> {
        val featuresSums = ArrayList<StatsSectionItem>()
        for (index in AudioFeatureType.values().indices) {
            val usersTracksValue = calculateDifference(
                AudioFeatureType.values()[index],
                topTracksWithFeatures.sumOf { it.features.at(index)!! } / topTracksWithFeatures.size
            ) * 100
            val generalTracksValue = calculateDifference(
                AudioFeatureType.values()[index],
                generalSetTracksWithFeatures.sumOf { it.features.at(index)!! } / generalSetTracksWithFeatures.size
            ) * 100
            featuresSums.add(StatsSectionItem(
                name = AudioFeatureType.values()[index].name,
                value = usersTracksValue - generalTracksValue
            ))
        }
        return featuresSums.sortedByDescending { it.value }.toMutableList()
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
    private fun getMostPopularTracks(): List<StatsSectionItem> {
        return topTracksWithFeatures.sortedByDescending { it.track.popularity }.map {
            StatsSectionItem(
                trackId = it.track.trackId,
                name = it.track.trackName,
                value = it.track.popularity.toDouble(),
                artistName = it.track.artists[0].artistName,
                imageUrl = it.track.album.albumImages[0].url
            )
        }
    }

    /**
     * Returns tracks from user's tracks by popularity in the ascending order
     */
    private fun getLeastPopularTracks(): List<StatsSectionItem> {
        return topTracksWithFeatures.sortedBy { it.track.popularity }.map {
            StatsSectionItem(trackId = it.track.trackId,
                name = it.track.trackName,
                value = it.track.popularity.toDouble(),
                artistName = it.track.artists[0].artistName,
                imageUrl = it.track.album.albumImages[0].url)
        }.filter { it.value > 0 }
    }

    /**
     * Returns tracks from user's tracks by popularity in the descending order
     */
    private fun getMostPopularArtists(): List<StatsSectionItem> {
        return topArtists.sortedByDescending { it.artistPopularity }.map {
            StatsSectionItem(name = it.artistName, value = it.artistPopularity?.toDouble() ?: 0.0, imageUrl = if (it.images != null && it.images!!.isNotEmpty()) it.images!![0].url else "")
        }
    }

    /**
     * Returns tracks from user's tracks by popularity in the ascending order
     */
    private fun getLeastPopularArtists() : List<StatsSectionItem>  {
        return topArtists.sortedBy { it.artistPopularity }.map {
            StatsSectionItem(name = it.artistName, value = it.artistPopularity?.toDouble() ?: 0.0, imageUrl = if (it.images != null && it.images!!.isNotEmpty()) it.images!![0].url else "")
        }.filter { it.value > 0 }.toMutableList()
    }

    private suspend fun getGeneralSetTracks(){
        Log.e("random", "here")
        val generalTracks = mutableListOf<Track>()
        val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        for (index in 0 until 10){
            val randomChar = Helper.random.nextInt(0, charPool.size)
            val offset = Helper.random.nextInt(0, 100)
            generalTracks.addAll(ApiRepository.getSearchTracks("%${charPool[randomChar]}%",  limit = 2, offset = offset, "track", context!!) ?: mutableListOf())
        }
        val generalWithFeatures = ApiRepository.getTracksAudioFeatures(generalTracks, context!!)
        if (generalWithFeatures != null) {
            insertRandomTracks(generalWithFeatures.toMutableList())
            generalSetTracksWithFeatures = generalWithFeatures.toMutableList()
        }
        Log.e("random", generalWithFeatures?.toString() ?: "null")
    }

    /**
     * Prepares homepage sections
     */
    private fun prepareSections() {
        Log.e("prepare", "here")
        val pagesList = mutableListOf<PageItem>()
        pagesList.add(PageItem(context!!.getString(R.string.our_recommendations), context!!.getString(R.string.home_our_recommend_content), PageType.RECOMMEND))
        pagesList.add(PageItem(context!!.getString(R.string.your_music), context!!.getString(R.string.home_your_music_content), PageType.USERTOP))
        pagesList.add(PageItem(context!!.getString(R.string.spotify_recommend_text), context!!.getString(R.string.home_spotify_recommend_content), PageType.SPOTIFY))
        pagesList.add(PageItem(context!!.getString(R.string.playlist_creation), context!!.getString(R.string.home_playlist_creation), PageType.PLAYLIST))

        val wordBundles = mutableListOf<WordItemBundle>()
        wordBundles.add(WordItemBundle(context!!.getString(R.string.top_genres_lc), topGenres.map { WordItem(it.name, min((it.value*1.25).toInt() + 8, 28)) }.toMutableList()))
        wordBundles.add(WordItemBundle(context!!.getString(R.string.top_artists), topArtists.map { WordItem(it.artistName, ((it.artistPopularity ?: 1)*0.3).toInt() + 2) }.toMutableList()))

        val statsSectionList = mutableListOf<StatsSection>()
        statsSectionList.add(StatsSection(StatsSectionType.MOSTPOP, getMostPopularTracks(), context!!.getString(
                    R.string.tracks_home_dropdown), "Popularity of track calculated by Spotify"))
        statsSectionList.add(StatsSection(StatsSectionType.MOSTPOP, getMostPopularArtists(), context!!.getString(
                    R.string.artists_home_dropdown), "Popularity of artist, calculated from popularity of artist's tracks by Spotify"))

        val statsSectionList2 = mutableListOf<StatsSection>()
        statsSectionList2.add(StatsSection(StatsSectionType.LEASTPOP, getLeastPopularTracks(), context!!.getString(
            R.string.tracks_home_dropdown), "Popularity of track calculated by Spotify"))
        statsSectionList2.add(StatsSection(StatsSectionType.LEASTPOP, getLeastPopularArtists(), context!!.getString(
            R.string.artists_home_dropdown), "Popularity of artist, calculated from popularity of artist's tracks by Spotify"))

        val statsSectionList3 = mutableListOf<StatsSection>()
        statsSectionList3.add(StatsSection(StatsSectionType.GENRE, topGenres, "Genres", "Occurrence of genre in user's music"))
        statsSectionList3.add(StatsSection(StatsSectionType.FEATURES, getTopAudioFeatures(), "Audio features", "Average value of audio feature from user's music relative to average value from \"random\" set of tracks"))

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
        val topTracksList = ApiRepository.getUserTopTracks(Config.topItemsLimit, context as MainActivity) ?: mutableListOf()
        val sizeDifference = Config.topItemsLimit - topTracksList.size
        if (sizeDifference > 0) {
            val savedTracks = ApiRepository.getUserSavedTracks(Config.savedTracksLimit, context!!) ?: mutableListOf()
            val savedTracksNew = savedTracks.filter { !topTracksList.contains(it) }
            val savedTracksFinal = savedTracksNew.take(min(sizeDifference, savedTracksNew.size))
            topTracksList.addAll(savedTracksFinal)
        }
        val userTracks = topTracksList.toMutableList()
        topTracksWithFeatures = ApiRepository.getTracksAudioFeatures(userTracks, context!!)?.toMutableList() ?: listOf()
    }

    /**
     * Gathers user's top artists from Spotify topArtists and savedTracks
     */
    private suspend fun getUserTopArtists() {
        val topArtistsList = ApiRepository.getUserTopArtists(Config.topItemsLimit, context!!) ?: mutableListOf()
        val sizeDifference = Config.topItemsLimit - topArtistsList.size
        if (sizeDifference > 0) {
            val savedTracks = ApiRepository.getUserSavedTracks(Config.savedTracksLimit, context!!) ?: mutableListOf()
            val savedArtists = savedTracks.map { it.artists[0] }.filter { !topArtistsList.contains(it) }
            val frequencyMap = mutableMapOf<Artist, Int>()
            for (artist in savedArtists.distinct()) {
                frequencyMap[artist] = Collections.frequency(savedArtists, artist)
            }
            val sortedArtists = frequencyMap.toList().sortedBy { (_, value) -> value }.map { it.first }
            val sortedArtistsFinal = ApiRepository.getArtistsDetail(
                sortedArtists.take(
                    min(
                        sizeDifference,
                        sortedArtists.size
                    )
                ).toMutableList(),
                context!!
            )
            topArtistsList.addAll(sortedArtistsFinal?.artists?.toMutableList() ?: mutableListOf())
        }
        topArtists = topArtistsList
    }

    /**
     * Gathers related artist for user's top artists
     */
    private suspend fun getUsersArtistsRelatedArtist() {
        for (artist in topArtists) {
            val relatedArtists = ApiRepository.getRelatedArtists(artist.artistId, context!!)
            topArtists[topArtists.indexOf(artist)].related_artists = relatedArtists?.toList() ?: mutableListOf()
        }
    }

    /**
     * Assigns related artist to each track's artist
     */
    private suspend fun getTracksRelatedArtists() {
        for (track in topTracksWithFeatures) {
            var trackArtist = (context as MainActivity).viewModel.topArtists.value?.firstOrNull { it.artistId == track.track.artists[0].artistId }
            if (trackArtist == null) {
                trackArtist = topTracksWithFeatures.firstOrNull { it.track.artists[0].artistId == track.track.artists[0].artistId }?.track?.artists?.get(0)
            }
            track.track.artists[0].related_artists = trackArtist?.related_artists
                ?: (ApiRepository.getRelatedArtists(track.track.artists[0].artistId, context!!)
                    ?.toList() ?: mutableListOf())
        }
        val response = ApiRepository.getArtistWithGenres(topTracksWithFeatures.map { it.track }.toMutableList(), context!!) ?: return
        for (artistIndex in response.artists.indices) {
            topTracksWithFeatures[artistIndex].track.trackGenres = response.artists[artistIndex].genres
            topTracksWithFeatures[artistIndex].track.artists[0].genres = response.artists[artistIndex].genres
            topTracksWithFeatures[artistIndex].track.artists[0].artistPopularity = response.artists[artistIndex].artistPopularity
            topTracksWithFeatures[artistIndex].track.artists[0].images = response.artists[artistIndex].images
        }
        insertUserItems(topTracksWithFeatures, topArtists)
    }
}