package com.example.spotifyexplained.general

import android.content.Context
import android.graphics.Color
import com.example.spotifyexplained.model.*
import com.example.spotifyexplained.services.ApiHelper
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min

object ColorHelper {
    private fun prepareGenresGroups(availableGenres : GenreSeeds, genresList: List<Array<String>>) : MutableList<GenresGroup>{
        val genreGroups = mutableListOf<GenresGroup>()
        for (genres in genresList){
            for (genre in genres) {
                if (isPartOfGroup(genre, genreGroups.map { it.name }, genreGroups)) {
                    continue
                }
                if (isPartOfGroup(genre, availableGenres.seeds.toList(), genreGroups)) {
                    continue
                }
                genreGroups.add(GenresGroup(genre, mutableListOf(genre)))
            }
        }
        return genreGroups
    }

    private fun isPartOfGroup(genre: String, genresList: List<String>, genreGroups: MutableList<GenresGroup>) : Boolean{
        val genreSet = genre.replace("&", "-n-")
            .replace("hip hop", "hip-hop")
            .replace("indie folk", "indie-folk")
            .split(" ").toSet()
        var matchFound = false
        for (genreSeed in genresList) {
            if (genreSet.contains(genreSeed) || genre.contains(genreSeed)){
                matchFound = true
                if (genreGroups.firstOrNull { it.name == genreSeed } == null) {
                    genreGroups.add(GenresGroup(genreSeed, mutableListOf(genre)))
                } else {
                    genreGroups.first { it.name == genreSeed }.items.add(genre)
                }
            }
        }
        return matchFound
    }

    fun assignColorToGenreGroups(numberOfGroups: Int): List<MixableColor>{
        val colorList = mutableListOf<MixableColor>()
        for (i in 0..numberOfGroups) {
            val hsvColor = (floatArrayOf(fmod(i * 0.618033988749895, 1.0).toFloat()*360, 0.75f, 1.0f))
            val color = Color.HSVToColor(hsvColor)
            colorList.add(MixableColor(Color.red(color), Color.green(color), Color.blue(color), 1.0))
        }
        return colorList
    }

    private fun fmod(a: Double, b: Double): Double {
        val result = floor(a / b).toInt()
        return a - result * b
    }

    fun getArtistColor(genres: Array<String>?, genreColorMap : HashMap<String, MixableColor>): String {
        val colorArray = ArrayList<MixableColor>()
        if (genres.isNullOrEmpty()){
            return Config.defaultColor
        }
        for (genre in genres) {
            if (genreColorMap[genre] != null) {
                colorArray.add(genreColorMap[genre]!!)
            }
        }
        val finalColor = Helper.mixColors(colorArray)
        return String.format("rgba(${finalColor.r}, ${finalColor.g}, ${finalColor.b}, ${finalColor.a})")
    }

    private fun removeExtraGenreGroups(genreGroups : MutableList<GenresGroup>){
        val extraGroups = mutableListOf<GenresGroup>()
        for (currentGroup in genreGroups){
            val groupsWithoutCurrent = genreGroups.filter { genreGroups.indexOf(it) > genreGroups.indexOf(currentGroup)}
            for (group in groupsWithoutCurrent){
                if (currentGroup.contains(group)){
                    extraGroups.add(group)
                } else if (currentGroup.name.length > 5 && currentGroup.sharePartOfName(group)){
                    currentGroup.items.addAll(group.items)
                    extraGroups.add(group)
                }
            }
        }
        genreGroups.removeAll { extraGroups.contains(it) }
    }

    suspend fun gatherColorsForGenres(artists : List<Artist>, context: Context) : HashMap<String, MixableColor> {
        val availableGenreSeeds = ApiHelper.getAvailableGenreSeeds(context) ?: GenreSeeds(arrayOf())
        val frequencyMap: MutableMap<String, Int> = HashMap()
        for (artist in artists) {
            if (artist.genres != null && artist.genres!!.isNotEmpty()) {
                for (genre in artist.genres!!) {
                    var count = frequencyMap[genre]
                    if (count == null) count = 0
                    frequencyMap[genre] = count + 1
                }
            }
        }
        val genreGroups = prepareGenresGroups(availableGenreSeeds, artists.map { it.genres ?: arrayOf() }).sortedByDescending { it.items.size }.toMutableList()
        removeExtraGenreGroups(genreGroups)
        val colorList = assignColorToGenreGroups(genreGroups.size)
        val genreColorMap: HashMap<String, MixableColor> = HashMap()
        for (i in 0 until genreGroups.count()) {
            val colorIndex = i % colorList.count()
            for (genre in genreGroups[i].items.distinct()) {
                if (genreGroups[i].name == genre) {
                    genreColorMap[genre] = MixableColor(
                        colorList[colorIndex].r,
                        colorList[colorIndex].g,
                        colorList[colorIndex].b,
                        1.0
                    )
                } else {
                    val randomR = Helper.random.nextInt(-4, 5)
                    val randomG = Helper.random.nextInt(-4, 5)
                    val randomB = Helper.random.nextInt(-4, 5)
                    genreColorMap[genre] = MixableColor(
                        max(min(colorList[colorIndex].r + randomR * 6, 255), 0),
                        max(min(colorList[colorIndex].g + randomG * 6, 255), 0),
                        max(min(colorList[colorIndex].b + randomB * 6, 255), 0),
                        1.0
                    )
                }
            }
        }
        return genreColorMap
    }
}