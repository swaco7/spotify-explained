package com.example.spotifyexplained.model

import com.example.spotifyexplained.model.enums.AudioFeatureType
import com.google.gson.annotations.SerializedName

data class AudioFeatures(
    @SerializedName("id")
    var featuresId: String,

    @SerializedName("acousticness")
    var acousticness: Double?,

    @SerializedName("danceability")
    var danceability: Double?,

    @SerializedName("energy")
    var energy: Double?,

    @SerializedName("instrumentalness")
    var instrumentalness: Double?,

    @SerializedName("liveness")
    var liveness: Double?,

    @SerializedName("loudness")
    var loudness: Double?,

    @SerializedName("speechiness")
    var speechiness: Double?,

    @SerializedName("tempo")
    var tempo: Double?,

    @SerializedName("valence")
    var valence: Double?,
) {
    constructor(featuresId: String) : this(featuresId,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null) {
        this.featuresId = featuresId
    }

    constructor(featuresId: String, featuresValues: List<Double>) : this(featuresId,
        featuresValues[0],
        featuresValues[1],
        featuresValues[2],
        featuresValues[3],
        featuresValues[4],
        featuresValues[5],
        featuresValues[6],
        featuresValues[7],
        featuresValues[8]) {
        this.acousticness = featuresValues[0]
        this.danceability = featuresValues[1]
        this.energy = featuresValues[2]
        this.instrumentalness = featuresValues[3]
        this.liveness = featuresValues[4]
        this.loudness = featuresValues[5]
        this.speechiness = featuresValues[6]
        this.tempo = featuresValues[7]
        this.valence = featuresValues[8]
        this.featuresId = featuresId
    }

    fun at(i: Int): Double? {
        return when (i) {
            0 -> acousticness
            1 -> danceability
            2 -> energy
            3 -> instrumentalness
            4 -> liveness
            5 -> loudness
            6 -> speechiness
            7 -> tempo
            8 -> valence
            else -> 0.0
        }
    }

    fun getPair(i: Int): Pair<String, Double?> {
        return when (i) {
            0 -> Pair(AudioFeatureType.ACOUSTICNESS.value, acousticness)
            1 -> Pair(AudioFeatureType.DANCEABILITY.value, danceability)
            2 -> Pair(AudioFeatureType.ENERGY.value, energy)
            3 -> Pair(AudioFeatureType.INSTRUMENTALNESS.value, instrumentalness)
            4 -> Pair(AudioFeatureType.LIVENESS.value, liveness)
            5 -> Pair(AudioFeatureType.LOUDNESS.value, loudness)
            6 -> Pair(AudioFeatureType.SPEECHINESS.value, speechiness)
            7 -> Pair(AudioFeatureType.TEMPO.value, tempo)
            8 -> Pair(AudioFeatureType.VALENCE.value, valence)
            else -> Pair("", 0.0)
        }
    }

    fun asList() : List<Pair<String, Double?>>{
        val features = mutableListOf<Pair<String, Double?>>()
        for (index in 0 until this.count()){
            features.add(getPair(index))
        }
        return features
    }

    fun set(i: Int, value: Double){
        when (i) {
            0 -> acousticness = value
            1 -> danceability = value
            2 -> energy = value
            3 -> instrumentalness = value
            4 -> liveness = value
            5 -> loudness = value
            6 -> speechiness = value
            7 -> tempo = value
            8 -> valence = value
            else -> return
        }
    }
    fun getValue(feature: String) : Double? {
        return when (feature) {
            AudioFeatureType.ACOUSTICNESS.value -> acousticness
            AudioFeatureType.DANCEABILITY.value -> danceability
            AudioFeatureType.ENERGY.value -> energy
            AudioFeatureType.INSTRUMENTALNESS.value -> instrumentalness
            AudioFeatureType.LIVENESS.value -> liveness
            AudioFeatureType.LOUDNESS.value -> loudness
            AudioFeatureType.SPEECHINESS.value -> speechiness
            AudioFeatureType.TEMPO.value -> tempo
            else -> valence
        }
    }

    fun getType(i : Int): AudioFeatureType {
        return when (i) {
            0 -> AudioFeatureType.ACOUSTICNESS
            1 -> AudioFeatureType.DANCEABILITY
            2 -> AudioFeatureType.ENERGY
            3 -> AudioFeatureType.INSTRUMENTALNESS
            4 -> AudioFeatureType.LIVENESS
            5 -> AudioFeatureType.LOUDNESS
            6 -> AudioFeatureType.SPEECHINESS
            7 -> AudioFeatureType.TEMPO
            else -> AudioFeatureType.VALENCE
        }
    }

    fun getName(i: Int): String {
        return when (i) {
            0 -> AudioFeatureType.ACOUSTICNESS.value
            1 -> AudioFeatureType.DANCEABILITY.value
            2 -> AudioFeatureType.ENERGY.value
            3 -> AudioFeatureType.INSTRUMENTALNESS.value
            4 -> AudioFeatureType.LIVENESS.value
            5 -> AudioFeatureType.LOUDNESS.value
            6 -> AudioFeatureType.SPEECHINESS.value
            7 -> AudioFeatureType.TEMPO.value
            8 -> AudioFeatureType.VALENCE.value
            else -> ""
        }
    }

    fun getIndex(feature: String): Int {
        return when (feature) {
            AudioFeatureType.ACOUSTICNESS.value -> 0
            AudioFeatureType.DANCEABILITY.value -> 1
            AudioFeatureType.ENERGY.value -> 2
            AudioFeatureType.INSTRUMENTALNESS.value -> 3
            AudioFeatureType.LIVENESS.value -> 4
            AudioFeatureType.LOUDNESS.value -> 5
            AudioFeatureType.SPEECHINESS.value -> 6
            AudioFeatureType.TEMPO.value -> 7
            else -> 8
        }
    }

    fun count() : Int{
        return 9
    }
}
