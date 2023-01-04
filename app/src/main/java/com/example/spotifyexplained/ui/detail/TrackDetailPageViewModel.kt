package com.example.spotifyexplained.ui.detail

import android.app.Activity
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spotifyexplained.R
import com.example.spotifyexplained.activity.MainActivity
import com.example.spotifyexplained.database.entity.RandomTrackEntity
import com.example.spotifyexplained.general.ColorHelper
import com.example.spotifyexplained.general.Config
import com.example.spotifyexplained.general.Helper
import com.example.spotifyexplained.model.*
import com.example.spotifyexplained.model.enums.LoadingState
import com.example.spotifyexplained.repository.TrackRepository
import com.example.spotifyexplained.services.ApiRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * ViewModel for playlist functionality
 * @param [activity] reference to MainActivity
 * @property [trackId] id of the track
 */
class TrackDetailPageViewModel(activity: Activity, private var trackId: String, private val repository: TrackRepository) : ViewModel() {
    val loadingState = MutableLiveData<LoadingState>().apply { value = LoadingState.LOADING }
    val isPremium = MutableLiveData<Boolean>().apply {}
    val isSaved = MutableLiveData<Boolean>().apply { value = false }
    val currentTrackUri = MutableLiveData<String>().apply { value = "" }
    val currentTrackTitle = MutableLiveData<String>().apply { value = "" }

    val topTracks: MutableLiveData<List<Track>> by lazy {
        MutableLiveData<List<Track>>(mutableListOf())
    }
    val radarData = MutableLiveData<RadarChartData>().apply {
        value = RadarChartData(ArrayList(), mutableListOf())
    }
    val barData = MutableLiveData<List<DataUnit>>().apply {
        value = mutableListOf()
    }
    val recommendedTrack = MutableLiveData<Track>().apply {
        value = null
    }
    val similarTracks = MutableLiveData<List<SimilarTrack>>().apply {
        value = listOf()
    }
    val mostSimilarByAttribute = MutableLiveData<List<TrackValue>>().apply {
        value = mutableListOf()
    }

    private val fullRandomTracks: Flow<MutableList<RandomTrackEntity>> = repository.allRandomTracks

    private val context: Context? by lazy {
        activity
    }
    private var minMax: List<Pair<Double, Double>> = listOf()
    private var minMaxRelative: List<Pair<Double, Double>> = listOf()
    private var averageValues: MutableList<Double> = mutableListOf()
    private var generalAverageValues: MutableList<Double> = mutableListOf()
    private var topTracksWithFeatures : List<TrackAudioFeatures> = listOf()
    private var generalSetTracksWithFeatures: List<TrackAudioFeatures> = listOf()
    private var valuesRelative = false

    init {
        viewModelScope.launch {
            fullRandomTracks.collect { randomTracks ->
                if (randomTracks.isNotEmpty()) {
                    generalSetTracksWithFeatures = randomTracks.map { TrackAudioFeatures(it.track, it.features) }
                } else {
                    getGeneralSetTracks()
                }
                loadingState.value = LoadingState.LOADING
                getTrack()
                prepareSections()
                loadingState.value = LoadingState.SUCCESS
            }
        }
    }

    private fun insertRandomTracks(tracks: MutableList<TrackAudioFeatures>){
        viewModelScope.launch {
            repository.insertToRandomTracks(tracks.map { RandomTrackEntity(it.track.trackId, it.track, it.features) })
        }
    }

    /**
     * Get track object for trackId
     */
    private suspend fun getTrack() {
        recommendedTrack.value = ApiRepository.getTracks(trackId, context!!)
    }

    /**
     * Get audio features for tracks
     */
    private suspend fun prepareSections() {
        if (recommendedTrack.value == null) return
        val list = (context as MainActivity).viewModel.topTracks.value!! + (ApiRepository.getTracksAudioFeatures(listOf(recommendedTrack.value!!) , context!!)?.toMutableList() ?: mutableListOf())
        topTracksWithFeatures = list
        minMax = Helper.getMinMaxFeatures(list)
        averageValues = Helper.getAverageFeatures(list)
        generalAverageValues = Helper.getAverageFeatures(generalSetTracksWithFeatures)
        val mostSimilar = findMostSimilar(list)
        similarTracks.value = prepareMostSimilarTracks(mostSimilar)
        radarData.value = RadarChartData(mostSimilar, minMax, averageValues, generalAverageValues)
        val colors = ColorHelper.assignColorToGenreGroups(7)
        findMostSimilarByAttribute(list, 0, valuesRelative)
    }

    /**
     *  Get most similar tracks overall, add special elements - average user's and average general.
     */
    private fun prepareMostSimilarTracks(mostSimilar: List<TrackAudioFeatures>) : List<SimilarTrack> {
        val mostSimilarTracks = mutableListOf<SimilarTrack>()
        for (track in mostSimilar) {
            mostSimilarTracks.add(
                SimilarTrack(
                    track.track.trackName,
                    Config.colorArray[mostSimilar.indexOf(track)],
                    Helper.getSimilarity(topTracksWithFeatures.last(), track, minMax),
                    track
                )
            )
        }
        mostSimilarTracks.add(
            SimilarTrack(
                context!!.resources.getString(R.string.average_of_yours),
                Config.colorAvgOfUser,
                Helper.getSimilarity(topTracksWithFeatures.last(), averageValues, minMax)
            )
        )
        mostSimilarTracks.add(
            SimilarTrack(
                context!!.resources.getString(R.string.average_general),
                Config.colorAvgGeneral,
                Helper.getSimilarity(topTracksWithFeatures.last(), generalAverageValues, minMax)
            )
        )
        return mostSimilarTracks
    }

    /**
     * Find most similar tracks to the selected track
     * @param [features] list of candidates
     * @return list of the most similar tracks
     */
    private fun findMostSimilar(features: List<TrackAudioFeatures>): MutableList<TrackAudioFeatures> {
        val map = mutableListOf<Pair<TrackAudioFeatures, Double>>()
        minMaxRelative = Helper.getMinMaxFeaturesRelative(features)
        for (track in features.dropLast(1)) {
            map.add(Pair(track, Helper.compareTrackFeatures(features.last(), track, minMaxRelative)))
        }
        val result = map.filter { it.first.track != features.last().track}.sortedBy { (_, value) -> value }.map { it.first }.take(Config.detailAllCount - 1)
        val mostSimilarList = mutableListOf<TrackAudioFeatures>()
        mostSimilarList.addAll(0, result)
        if (map.count() > Config.detailAllCount) {
            mostSimilarList.addAll(map.sortedBy { (_, value) -> value }.map { it.first }
                .takeLast(1))
        }
        mostSimilarList.add(0, features.last())
        return mostSimilarList.toMutableList()
    }

    fun valueSwitchChanged(value : Boolean, position: Int){
        valuesRelative = value
        findMostSimilarByAttribute(position)
    }

    private suspend fun getGeneralSetTracks(){
        val generalTracks = mutableListOf<Track>()
        val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        for (index in 0 until 10){
            val randomChar = Helper.random.nextInt(0, charPool.size)
            val offset = Helper.random.nextInt(0, 100)
            generalTracks.addAll(ApiRepository.getSearchTracks("%${charPool[randomChar]}%",  limit = 2, offset = offset, "track", context!!)!!)
        }
        val generalWithFeatures = ApiRepository.getTracksAudioFeatures(generalTracks, context!!)
        insertRandomTracks(generalWithFeatures!!.toMutableList())
    }

    fun findMostSimilarByAttribute(index: Int) {
        findMostSimilarByAttribute(topTracksWithFeatures, index, valuesRelative)
    }

    /**
     * Find most similar tracks by attribute
     * @param tracks from user's music
     * @param selectedFeatureIndex index of selected type of feature (acousticness == 0,...)
     * @param relative flag whether we compute absolute or relative value
     */
    private fun findMostSimilarByAttribute(tracks: List<TrackAudioFeatures>, selectedFeatureIndex: Int, relative: Boolean) {
        if (recommendedTrack.value == null) return
        val generalFeatureValue = generalAverageValues[selectedFeatureIndex]
        val mappedTracks = tracks.dropLast(1).map { TrackValue(
            it.track.trackId,
            it.track.trackName,
            it.track.artists[0].artistName,
            it.track.album.albumImages[0].url,
            (1 - normalizedValue(
                it.features.at(selectedFeatureIndex)!!,
                tracks.last().features.at(selectedFeatureIndex)!!,
                selectedFeatureIndex,
                relative,
                generalFeatureValue,
            )) * 100
        ) }.sortedWith(compareByDescending { it.value
        })
        val sortedTracks = mappedTracks.take(8).toMutableList()
        sortedTracks.add(TrackValue(
            name = context!!.getString(R.string.average_of_yours),
            value = (1 - normalizedValue(averageValues[selectedFeatureIndex], tracks.last().features.at(selectedFeatureIndex)!!, selectedFeatureIndex, relative, generalFeatureValue))*100
        ))
        sortedTracks.add(TrackValue(
            name = context!!.getString(R.string.average_general),
            value = (1 - normalizedValue(generalAverageValues[selectedFeatureIndex], tracks.last().features.at(selectedFeatureIndex)!!, selectedFeatureIndex, relative, generalFeatureValue))*100
        ))
        mostSimilarByAttribute.value = sortedTracks.drop(1).toMutableList()
    }

    /**
     * @return based on relative flag, absolute or relative similarity of normalized values (to interval 0..1) for selected audio feature type
     */
    private fun normalizedValue(sourceValue: Double, targetValue: Double, index: Int, relative: Boolean, generalFeatureValue: Double): Double {
        val first = Helper.normalize(sourceValue, minMaxRelative[index].first, minMaxRelative[index].second)
        val second = Helper.normalize(targetValue, minMaxRelative[index].first, minMaxRelative[index].second)
        val generalFeatureValueNormalized = Helper.normalize(generalFeatureValue, minMaxRelative[index].first, minMaxRelative[index].second)
        return if (relative) {
            abs(first - second) / abs(second-generalFeatureValueNormalized)
        } else {
            abs(first - second)
        }
    }
}