package com.example.spotifyexplained.model

data class RadarChartData(val tracksWithFeatures: List<TrackAudioFeatures>, val minMax: List<Pair<Double, Double>>,  val averageValues: List<Double>? = null, val averageGeneralValues: List<Double>? = null) {
    private lateinit var mapMinMax : MutableMap<Any, Pair<Double, Double>>

    override fun toString(): String {
        getMinMax()
        val builder = StringBuilder()
        builder.append("[\n")
        for (trackWithFeatures in tracksWithFeatures) {
            prepareItem(builder, trackWithFeatures.features)
        }
        if (averageValues != null && averageValues.isNotEmpty()){
            prepareItem(builder, AudioFeatures("average", averageValues))
        }
        if (averageGeneralValues != null && averageGeneralValues.isNotEmpty()){
            prepareItem(builder, AudioFeatures("averageGeneral", averageGeneralValues))
        }
        builder.removeSuffix(",")
        builder.append("]\n")
        return builder.toString()
    }

    fun prepareItem(builder: StringBuilder, features: AudioFeatures){
        builder.append("[\n")
        builder.append("{axis:\"${AudioFeatureType.ACOUSTICNESS}\",value:${normalize(AudioFeatureType.ACOUSTICNESS, features.acousticness!!)}},\n")
        builder.append("{axis:\"${AudioFeatureType.DANCEABILITY}\",value:${normalize(AudioFeatureType.DANCEABILITY, features.danceability!!)}},\n")
        builder.append("{axis:\"${AudioFeatureType.ENERGY}\",value:${normalize(AudioFeatureType.ENERGY, features.energy!!)}},\n")
        builder.append("{axis:\"${AudioFeatureType.INSTRUMENTALNESS}\",value:${normalize(AudioFeatureType.INSTRUMENTALNESS, features.instrumentalness!!)}},\n")
        builder.append("{axis:\"${AudioFeatureType.LIVENESS}\",value:${normalize(AudioFeatureType.LIVENESS, features.liveness!!)}},\n")
        builder.append("{axis:\"${AudioFeatureType.LOUDNESS}\",value:${normalize(AudioFeatureType.LOUDNESS, features.loudness!!)}},\n")
        builder.append("{axis:\"${AudioFeatureType.SPEECHINESS}\",value:${normalize(AudioFeatureType.SPEECHINESS, features.speechiness!!)}},\n")
        builder.append("{axis:\"${AudioFeatureType.TEMPO}\",value:${normalize(AudioFeatureType.TEMPO, features.tempo!!)}},\n")
        builder.append("{axis:\"${AudioFeatureType.VALENCE}\",value:${normalize(AudioFeatureType.VALENCE, features.valence!!)}}\n")
        builder.append("],\n")
    }

    fun tracksToString(): String {
        val builder = StringBuilder()
        builder.append("[\n")
        for (feature in tracksWithFeatures) {
            builder.append("\"${feature.track?.trackName ?: feature.features.featuresId} - ${feature.track?.artists?.get(0)?.artistName ?: ""}\",\n")
        }
        builder.removeSuffix(",")
        builder.append("]\n")
        return builder.toString()
    }

    private fun getMinMax() {
        mapMinMax = mutableMapOf()
        mapMinMax[AudioFeatureType.ACOUSTICNESS] = Pair(0.0 , 1.0)
        mapMinMax[AudioFeatureType.DANCEABILITY] = Pair(0.0 , 1.0)
        mapMinMax[AudioFeatureType.ENERGY] = Pair(0.0, 1.0)
        mapMinMax[AudioFeatureType.INSTRUMENTALNESS] = Pair(0.0, 1.0)
        mapMinMax[AudioFeatureType.LIVENESS] = Pair(0.0, 1.0)
        mapMinMax[AudioFeatureType.LOUDNESS] = Pair(tracksWithFeatures.minByOrNull { it.features.loudness!! }!!.features.loudness!!,
            tracksWithFeatures.maxByOrNull { it.features.loudness!! }!!.features.loudness!!)
        mapMinMax[AudioFeatureType.SPEECHINESS] = Pair(0.0, 1.0)
        mapMinMax[AudioFeatureType.TEMPO] = Pair(tracksWithFeatures.minByOrNull { it.features.tempo!! }!!.features.tempo!!,
            tracksWithFeatures.maxByOrNull { it.features.tempo!! }!!.features.tempo!!)
        mapMinMax[AudioFeatureType.VALENCE] = Pair(0.0, 1.0)
    }

    private fun normalize(featureType: AudioFeatureType, rawValue: Double) : Double {
        return (rawValue - minMax[featureType.ordinal].first) / (minMax[featureType.ordinal].second - minMax[featureType.ordinal].first)
    }
}
