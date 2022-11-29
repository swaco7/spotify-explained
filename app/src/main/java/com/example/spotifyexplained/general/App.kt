package com.example.spotifyexplained.general

import android.app.Application
import android.content.Context
import com.example.spotifyexplained.repository.TrackRepository
import com.example.spotifyexplained.database.TrackRoomDatabase

class App: Application() {
    companion object {
        lateinit var context: Context
    }

    val database by lazy { TrackRoomDatabase.getDatabase(this) }
    val repository by lazy { TrackRepository(database.trackDao()) }

    override fun onCreate() {
        super.onCreate()
        context = this
    }

}