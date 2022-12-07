package com.example.spotifyexplained.general

import com.example.spotifyexplained.model.GraphSettings

object Config {
    const val BASE_TOKEN_URL = "https://accounts.spotify.com/"
    const val BASE_URL = "https://api.spotify.com/v1/"
    const val CLIENT_ID = "e913b8d4c7584112b8f6972ad3c34360"
    const val REDIRECT_URI = "spotify-sdk://auth"
    const val AUTH_TOKEN_REQUEST_CODE = 0x10
    const val jsAppName = "androidApp"
    const val mimeType = "text/html"
    const val encoding = "base64"

    //Settings
    const val defaultColor = "#0080ff"
    const val colorAvgOfUser = "#FFFF00"
    const val colorAvgGeneral = "#FFA500"
    const val nodeColorTrack = "#0080FF"
    const val nodeColorFeature = "#9a76bd"
    const val recommendGraphPopularityFactor = 2
    const val featureNodeRadius = 30
    const val recommendedCount = 30
    const val savedTracksLimit = 50
    const val topItemsLimit = 50
    const val radius = 20
    const val trackRecommendManyBody = -20
    const val trackRecommendCollisions = 1.25f
    const val artistRecommendManyBody = -5
    const val artistRecommendCollisions = 1.33f
    const val genreRecommendManyBody = -5
    const val genreRecommendCollisions = 1.33f
    const val combinedRecommendManyBody = -10
    const val combinedRecommendCollisions = 1.5f
    const val topTracksManyBody = -5
    const val topTracksCollisions = 1.33f
    const val customRecommendOverallManyBody = -5
    const val trackSizeForCustomRecommend = 30
    const val baseLineWidth = 10
    const val nodeDegreeSizeFactor = 2
    const val nodeDegreeSizeConstant = 30
    const val jaccardTopTracks = 0.4
    const val jaccardArtists = 0.25
    const val jaccardTracks = 0.25
    const val featuresSimilarityThreshold = 0.975
    const val forceDistanceFactor = 10
    const val customRecommendTrackLimit = 10
    const val customSpecificTrackCount = 10

    val colorArray = arrayOf("#177ADF", "#2CBA3D",  "#2CB9BA", "#2C49BA", "#752DB4", "#CC333F")
    val manyBody = arrayOf(1, 5, 10, 25)
    val collisions = arrayOf(1.2f, 1.5f, 1.75f, 2f)
    val graphSettings = GraphSettings(
        artistsSelected = false,
        relatedFlag = true,
        genresFlag = true,
        featuresFlag = false,
        zoomFlag = true
    )
    const val metricsMode = true
}