package com.example.spotifyexplained.model

import com.google.gson.annotations.SerializedName

data class FeaturesList(
    @SerializedName("audio_features")
    val audio_features: Array<AudioFeatures>
    )
