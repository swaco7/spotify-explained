package com.example.spotifyexplained.model

import androidx.lifecycle.MutableLiveData

data class GraphSettings(
    var artistsSelected : Boolean,
    var relatedFlag : Boolean,
    var genresFlag : Boolean,
    var featuresFlag: Boolean,
    var zoomFlag: Boolean
)
