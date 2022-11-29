package com.example.spotifyexplained.database

import androidx.room.TypeConverter
import com.example.spotifyexplained.model.Artist
import com.example.spotifyexplained.model.Image
import com.example.spotifyexplained.model.TrackAudioFeatures
import com.google.gson.Gson

import com.google.gson.reflect.TypeToken

class Converters {
    @TypeConverter
    fun fromString(string: String?): Array<String>? {
        val listType = object : TypeToken<Array<String?>?>() {}.type
        return Gson().fromJson(string, listType)
    }

    @TypeConverter
    fun fromArrayList(list: Array<String>?): String? {
        val gson = Gson()
        return gson.toJson(list)
    }

    @TypeConverter
    fun fromImageArray(arr: Array<Image>?): String? {
        val gson = Gson()
        return gson.toJson(arr)
    }

    @TypeConverter
    fun fromArtistArray(arr: Array<Artist>?): String? {
        val gson = Gson()
        return gson.toJson(arr)
    }

    @TypeConverter
    fun fromArtistList(arr: List<Artist>?): String? {
        val gson = Gson()
        return gson.toJson(arr)
    }

    @TypeConverter
    fun fromTracksList(arr: List<TrackAudioFeatures>?): String? {
        val gson = Gson()
        return gson.toJson(arr)
    }


    @TypeConverter
    fun toImageArray(json: String?): Array<Image>? {
        val listType = object : TypeToken<Array<Image?>?>() {}.type
        return Gson().fromJson(json, listType)
    }

    @TypeConverter
    fun toArtistArray(json: String?): Array<Artist>? {
        val listType = object : TypeToken<Array<Artist?>?>() {}.type
        return Gson().fromJson(json, listType)
    }

    @TypeConverter
    fun toArtistList(json: String?): List<Artist>? {
        val listType = object : TypeToken<List<Artist?>?>() {}.type
        return Gson().fromJson(json, listType)
    }

    @TypeConverter
    fun toTrackAudioFeaturesList(json: String?): List<TrackAudioFeatures>? {
        val listType = object : TypeToken<List<TrackAudioFeatures?>?>() {}.type
        return Gson().fromJson(json, listType)
    }
}