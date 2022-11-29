package com.example.spotifyexplained.ui.detail

import android.app.Activity
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.spotifyexplained.activity.MainActivity
import com.example.spotifyexplained.general.Helper
import com.example.spotifyexplained.model.*
import com.example.spotifyexplained.services.ApiHelper
import com.example.spotifyexplained.services.SessionManager
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.random.Random

/**
 * ViewModel for playlist functionality
 * @param [activity] reference to MainActivity
 * @property [trackId] id of the track
 */
class TrackDetailPageViewModel(activity: Activity, private var trackId: String) : ViewModel() {
    val loadingState = MutableLiveData<LoadingState>().apply { value = LoadingState.LOADING }
    val isPremium = MutableLiveData<Boolean>().apply {}
    val isSaved = MutableLiveData<Boolean>().apply { value = false }
    val currentTrackUri = MutableLiveData<String>().apply { value = "" }

    val topTracks: MutableLiveData<MutableList<Track>> by lazy {
        MutableLiveData<MutableList<Track>>(mutableListOf())
    }
    val audioFeatures: MutableLiveData<List<AudioFeatures>> by lazy {
        MutableLiveData<List<AudioFeatures>>(ArrayList())
    }
    val radarData = MutableLiveData<RadarChartData>().apply {
        value = RadarChartData(ArrayList(), mutableListOf())
    }
    val barData = MutableLiveData<MutableList<DataUnit>>().apply {
        value = mutableListOf()
    }
    val recommendedTrack = MutableLiveData<Track>().apply {
        value = null
    }
    val similarTracks = MutableLiveData<MutableList<SimilarTrack>>().apply {
        value = mutableListOf()
    }
    private val topTracksWithFeatures = MutableLiveData<MutableList<TrackAudioFeatures>>().apply {
        value = mutableListOf()
    }
    private val mostSimilar = MutableLiveData<MutableList<TrackAudioFeatures>>().apply {
        value = mutableListOf()
    }
    val mostSimilarByAttribute = MutableLiveData<MutableList<TrackValue>>().apply {
        value = mutableListOf()
    }
    private val generalSetTracksWithFeatures: MutableLiveData<MutableList<TrackAudioFeatures>> by lazy {
        MutableLiveData<MutableList<TrackAudioFeatures>>(mutableListOf())
    }
    val valuesRelative = MutableLiveData<Boolean>().apply {
        value = false
    }
    private lateinit var minMax: List<Pair<Double, Double>>
    private lateinit var minMaxRelative: List<Pair<Double, Double>>
    private lateinit var averageValues: MutableList<Double>
    private lateinit var generalAverageValues: MutableList<Double>
    private val random = Random(42)

    init {
        if (SessionManager.tokenExpired()) {
            (activity as MainActivity).authorizeUser()
        } else {
            viewModelScope.launch {
                loadingState.value = LoadingState.LOADING
                getGeneralSetTracks()
                getUserTopTracks()
                getTrack()
                getTracksAudioFeatures()
                loadingState.value = LoadingState.SUCCESS
            }
        }
    }

    private suspend fun getUserTopTracks() {
        val result = ApiHelper.getUserTopTracks(limit = 50) ?: mutableListOf()
        topTracks.value = result.toMutableList()
    }

    /**
     * Get track object for trackId
     */
    private suspend fun getTrack() {
        recommendedTrack.value = ApiHelper.getTracks(trackId)
    }

    /**
     * Get audio features for tracks
     */
    private suspend fun getTracksAudioFeatures() {
        val list = ApiHelper.getTracksAudioFeatures(topTracks.value!! + recommendedTrack.value!!)!!.toMutableList()
        topTracksWithFeatures.value = list
        minMax = Helper.getMinMaxFeatures(list)
        averageValues = Helper.getAverageFeatures(list)
        generalAverageValues = Helper.getAverageFeatures(generalSetTracksWithFeatures.value!!)
        mostSimilar.value = findMostSimilar(list)
        prepareMostSimilarTracks()
        radarData.value = RadarChartData(mostSimilar.value!!, minMax, averageValues, generalAverageValues)
        val colors = Helper.assignColorToGenreGroups(7)
        Log.e("colors", colors.toString())
        findMostSimilarByAttribute(list, 0, valuesRelative.value!!)
    }

    /**
     *
     */
    private suspend fun prepareMostSimilarTracks() {
        for (track in mostSimilar.value!!) {
            similarTracks.value!!.add(
                SimilarTrack(
                    track.track.trackName,
                    Constants.colorArray[mostSimilar.value!!.indexOf(track)],
                    Helper.getSimilarity(topTracksWithFeatures.value!!.last(), track, minMax),
                    track
                )
            )
        }
        similarTracks.value!!.add(
            SimilarTrack(
                "Average",
                "#FFFF00",
                Helper.getSimilarity(topTracksWithFeatures.value!!.last(), averageValues, minMax)
            )
        )
        similarTracks.value!!.add(
            SimilarTrack(
                "Average general",
                "#FFA500",
                Helper.getSimilarity(topTracksWithFeatures.value!!.last(), generalAverageValues, minMax)
            )
        )
    }

    /**
     * Find most similar tracks to the selected track
     * @param [features] list of candidates
     * @return list of the most similar tracks
     */
    private fun findMostSimilar(features: MutableList<TrackAudioFeatures>): MutableList<TrackAudioFeatures> {
        val map = mutableListOf<Pair<TrackAudioFeatures, Double>>()
        minMaxRelative = Helper.getMinMaxFeaturesRelative(features)
        for (track in features.dropLast(1)) {
            map.add(Pair(track, Helper.compareTrackFeatures(features.last(), track, minMaxRelative)))
        }
        val result = map.filter { it.first.track != features.last().track}.sortedBy { (_, value) -> value }.map { it.first }.take(4)
        val mostSimilarList = mutableListOf<TrackAudioFeatures>()
        mostSimilarList.addAll(0, result)
        mostSimilarList.addAll(map.sortedBy { (_, value) -> value }.map { it.first }.takeLast(1))
        mostSimilarList.add(0, features.last())
        return mostSimilarList.toMutableList()
    }

    fun valueSwitchChanged(value : Boolean, position: Int){
        valuesRelative.value = value
        findMostSimilarByAttribute(position)
    }

    /**
     * Find most similar tracks by single audio feature to the selected track, prepares data for horizontal bar chart
     * @param [tracks] list of candidates
     * @param [index] index of the audio feature
     */
//    private fun findMostSimilarByAttributeGraph(tracks: MutableList<TrackAudioFeatures>, index: Int) {
//        val sortedTracks = tracks.dropLast(1).sortedWith(compareBy {
//            normalizedValue(it.features.at(index)!!, tracks.last().features.at(index)!!, index)
//        }).take(8)
//        val dataForBarChart = sortedTracks.map {
//            DataUnit(
//                prepareTrackName(it.track.trackName),
//                (1 - normalizedValue(it.features.at(index)!!, tracks.last().features.at(index)!!, index))*100,
//                if (mostSimilar.value!!.contains(it)) 1 else 2
//            )
//        }.toMutableList()
//        dataForBarChart.add(
//            DataUnit(
//                "Average",
//                (1 - normalizedValue(averageValues[index], tracks.last().features.at(index)!!, index))*100,
//                2
//            ))
//        barData.value = dataForBarChart
//     }

    private suspend fun getGeneralSetTracks(){
        val generalTracks = mutableListOf<Track>()
        val charPool : List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        for (index in 0 until 10){
            val randomChar = random.nextInt(0, charPool.size)
            val offset = random.nextInt(0, 100)
            generalTracks.addAll(ApiHelper.getSearchTracks("%${charPool[randomChar]}%",  limit = 2, offset = offset, "track")!!)
        }
        val generalWithFeatures = ApiHelper.getTracksAudioFeatures(generalTracks)
        generalSetTracksWithFeatures.value = generalWithFeatures?.toMutableList()
    }

    fun findMostSimilarByAttribute(index: Int) {
        findMostSimilarByAttribute(topTracksWithFeatures.value!!, index, valuesRelative.value!!)
    }

    private fun findMostSimilarByAttribute(tracks: MutableList<TrackAudioFeatures>, index: Int, relative: Boolean) {
        val generalFeatureValue = generalAverageValues[index]
        val mappedTracks = tracks.dropLast(1).map { TrackValue(
            it.track.trackId,
            it.track.trackName,
            it.track.artists[0].artistName,
            it.track.album.albumImages[0].url,
            (1 - normalizedValue(
                it.features.at(index)!!,
                tracks.last().features.at(index)!!,
                index,
                relative,
                generalFeatureValue,
            )) * 100
        ) }.sortedWith(compareByDescending { it.value
        })
        val sortedTracks = mappedTracks.take(8).toMutableList()
        sortedTracks.add(TrackValue(
            name = "Average of yours",
            value = (1 - normalizedValue(averageValues[index], tracks.last().features.at(index)!!, index, relative,generalFeatureValue))*100
        ))
        sortedTracks.add(TrackValue(
            name = "Average general",
            value = (1 - normalizedValue(generalAverageValues[index], tracks.last().features.at(index)!!, index, relative,generalFeatureValue))*100
        ))
        mostSimilarByAttribute.value = sortedTracks.drop(1).toMutableList()
    }

//    private fun normalizedValue(sourceValue: Double, targetValue: Double, index: Int, relative: Boolean, generalFeatureValue: Double): Double {
//        val first = Helper.normalize(sourceValue, minMaxRelative[index].first, minMaxRelative[index].second)
//        val second = Helper.normalize(targetValue, minMaxRelative[index].first, minMaxRelative[index].second)
//        return if (relative) {
//            Log.e("test", "$first -- $second / $second -- $generalFeatureValue = ${abs(first - second) / abs(targetValue-generalFeatureValue)}")
//            abs(first - second) / abs(second-generalFeatureValue)
//        } else {
//            abs(first - second)
//        }
//    }



    private fun normalizedValue(sourceValue: Double, targetValue: Double, index: Int, relative: Boolean, generalFeatureValue: Double): Double {
        //Log.e("test", "$sourceValue -- $targetValue / $targetValue -- $generalFeatureValue = ${abs(sourceValue - targetValue) / abs(targetValue-generalFeatureValue)}")
        //return abs(sourceValue - targetValue) / abs(targetValue-generalFeatureValue)
        val first = Helper.normalize(sourceValue, minMaxRelative[index].first, minMaxRelative[index].second)
        val second = Helper.normalize(targetValue, minMaxRelative[index].first, minMaxRelative[index].second)
        val generalFeatureValueNormalized = Helper.normalize(generalFeatureValue, minMaxRelative[index].first, minMaxRelative[index].second)
        return if (relative) {
            Log.e("test", "$first -- $second / $second -- $generalFeatureValueNormalized = ${abs(first - second) / abs(targetValue-generalFeatureValueNormalized)}")
            abs(first - second) / abs(second-generalFeatureValueNormalized)
        } else {
            abs(first - second)
        }
    }

    /**
     * Shortens track name in case it is too long for horizontal chart
     * @param [trackName] to shorten
     * @return shortened track name
     */
    private fun prepareTrackName(trackName: String) : String {
        val split = trackName.splitToSequence(" ")
        val stringBuilder = StringBuilder()
        for (word in split) {
            if (stringBuilder.count() > 12) {
                return stringBuilder.toString()
            } else {
                stringBuilder.append(word).append(" ")
            }
        }
        return stringBuilder.toString()
    }
}