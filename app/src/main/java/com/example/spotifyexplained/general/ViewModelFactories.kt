package com.example.spotifyexplained.general

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.spotifyexplained.activity.MainViewModel
import com.example.spotifyexplained.repository.TrackRepository
import com.example.spotifyexplained.ui.detail.TrackDetailPageViewModel
import com.example.spotifyexplained.ui.home.HomeViewModel
import com.example.spotifyexplained.ui.playlist.PlaylistViewModel
import com.example.spotifyexplained.ui.recommend.custom.base.CustomRecommendBaseViewModel
import com.example.spotifyexplained.ui.recommend.custom.overall.CustomRecommendOverallViewModel
import com.example.spotifyexplained.ui.recommend.custom.settings.CustomRecommendSettingsViewModel
import com.example.spotifyexplained.ui.recommend.custom.specific.CustomRecommendSpecificViewModel
import com.example.spotifyexplained.ui.recommend.spotify.artists.ArtistRecommendViewModel
import com.example.spotifyexplained.ui.recommend.spotify.combined.CombinedRecommendViewModel
import com.example.spotifyexplained.ui.recommend.spotify.genre.GenresRecommendViewModel
import com.example.spotifyexplained.ui.recommend.spotify.tracks.TrackRecommendViewModel
import com.example.spotifyexplained.ui.topItems.SavedSongsViewModel
import com.example.spotifyexplained.ui.topItems.TopArtistsViewModel
import com.example.spotifyexplained.ui.topItems.TopTracksViewModel

class ContextViewModelFactory(private val context: Activity) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TopTracksViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TopTracksViewModel(context) as T
        }
        if (modelClass.isAssignableFrom(TopArtistsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TopArtistsViewModel(context) as T
        }
        if (modelClass.isAssignableFrom(SavedSongsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SavedSongsViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}

class TrackViewModelFactory(private val context: Activity, private val track: String, private val repository: TrackRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TrackDetailPageViewModel::class.java)){
            @Suppress("UNCHECKED_CAST")
            return TrackDetailPageViewModel(context, track, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class TrackDatabaseViewModelFactory(private val context: Activity, private val repository: TrackRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CustomRecommendOverallViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CustomRecommendOverallViewModel(context, repository) as T
        }

        if (modelClass.isAssignableFrom(ArtistRecommendViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ArtistRecommendViewModel(context, repository) as T
        }

        if (modelClass.isAssignableFrom(TrackRecommendViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TrackRecommendViewModel(context, repository) as T
        }

        if (modelClass.isAssignableFrom(GenresRecommendViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GenresRecommendViewModel(context, repository) as T
        }
        if (modelClass.isAssignableFrom(CombinedRecommendViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CombinedRecommendViewModel(context, repository) as T
        }
        if (modelClass.isAssignableFrom(CustomRecommendSpecificViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CustomRecommendSpecificViewModel(context, repository) as T
        }
        if (modelClass.isAssignableFrom(CustomRecommendSettingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CustomRecommendSettingsViewModel(context, repository) as T
        }
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel() as T
        }
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(context, repository) as T
        }
        if (modelClass.isAssignableFrom(PlaylistViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PlaylistViewModel(context, repository) as T
        }
        if (modelClass.isAssignableFrom(CustomRecommendBaseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CustomRecommendBaseViewModel(context, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}