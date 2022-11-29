package com.example.spotifyexplained.general

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import com.example.spotifyexplained.R
import com.example.spotifyexplained.model.*
import com.example.spotifyexplained.services.ApiHelper
import kotlin.math.*
import kotlin.random.Random

object Helper {
    val random = Random(42)

    fun getSkeletonColor(context : Context): Int{
        return when (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> context.resources.getColor(R.color.skeleton_darker, context.theme)
            else -> context.resources.getColor(R.color.skeleton_lighter, context.theme)
        }
    }

    fun compareTrackFeatures(recomTrack: TrackAudioFeatures, targetTrack: TrackAudioFeatures, minMax: List<Pair<Double, Double>>): Double  {
        var sum = 0.0
        for (index in 0 until recomTrack.features.count()) {
            val diff = abs(normalize(recomTrack.features.at(index)!!, minMax[index].first, minMax[index].second) - normalize(targetTrack.features.at(index)!!, minMax[index].first, minMax[index].second))
            sum += diff
        }
        return sum
    }

    fun compareTrackFeatures(recomTrack: TrackAudioFeatures, targetTrack: TrackAudioFeatures, minMax: List<Pair<Double, Double>>, featuresSettings: List<AudioFeature>): Double  {
        var sum = 0.0
        for (index in 0 until recomTrack.features.count()) {
            sum += abs(normalize(recomTrack.features.at(index)!!, minMax[index].first, minMax[index].second) -
                    normalize(targetTrack.features.at(index)!!, minMax[index].first, minMax[index].second)) *
                    (featuresSettings[index].value ?: 0.0)
        }
        return sum
    }

    fun compareTrackFeatures(recomTrack: TrackAudioFeatures, targetTrack: List<Double>, minMax: List<Pair<Double, Double>>): Double  {
        var sum = 0.0
        for (index in 0 until recomTrack.features.count()) {
            sum += abs(normalize(recomTrack.features.at(index)!!, minMax[index].first, minMax[index].second) -
                    normalize(targetTrack[index], minMax[index].first, minMax[index].second))
        }
        return sum
    }

    fun getSimilarity(recomTrack: TrackAudioFeatures, targetTrack: TrackAudioFeatures, minMax: List<Pair<Double, Double>>) : Double {
        val sum = compareTrackFeatures(recomTrack, targetTrack, minMax)
        return 1-sum/9
    }

    fun getSimilarity(recomTrack: TrackAudioFeatures, targetTrack: List<Double>, minMax: List<Pair<Double, Double>>) : Double {
        val sum = compareTrackFeatures(recomTrack, targetTrack, minMax)
        return 1-sum/9
    }

    fun normalize(rawValue: Double, min: Double, max: Double) : Double {
        return ((rawValue - min) / (max - min))
    }

    fun mixColors(colors: ArrayList<MixableColor>): MixableColor{
        val finalColor = MixableColor(
            colors.map { it.r }.average().toInt(),
            colors.map { it.g }.average().toInt(),
            colors.map { it.b }.average().toInt(),
            1.0
        )
        return finalColor
    }

    fun getMinMaxFeatures(trackWithFeatures: List<TrackAudioFeatures>): List<Pair<Double, Double>> {
        val list = mutableListOf<Pair<Double, Double>>()
        for (index in 0 until trackWithFeatures[0].features.count()){
            when (trackWithFeatures[0].features.getType(index)){
                AudioFeatureType.TEMPO -> list.add(index, Pair(40.0,200.0))
                AudioFeatureType.LOUDNESS -> list.add(index, Pair(-60.0, 0.0))
                else -> list.add(index, Pair(0.0, 1.0))
            }
        }
        return list
    }

    fun getMinMaxFeaturesRelative(trackWithFeatures: List<TrackAudioFeatures>): List<Pair<Double, Double>> {
        val list = mutableListOf<Pair<Double, Double>>()
        for (index in 0 until trackWithFeatures[0].features.count()){
           list.add(Pair(trackWithFeatures.minOf { it.features.at(index)!!}, trackWithFeatures.maxOf { it.features.at(index)!!}))
        }
        return list
    }

    fun getMinMaxFeaturesRelativeToAverage(trackWithFeatures: List<TrackAudioFeatures>): List<Pair<Double, Double>> {
        val list = mutableListOf<Pair<Double, Double>>()
        for (index in 0 until trackWithFeatures[0].features.count()){
            list.add(Pair(trackWithFeatures.minOf { it.features.at(index)!!}, trackWithFeatures.maxOf { it.features.at(index)!!}))
        }
        return list
    }

    fun getAverageFeatures(trackWithFeatures: MutableList<TrackAudioFeatures>): MutableList<Double> {
        val list = mutableListOf<Double>()
        for (index in 0 until trackWithFeatures[0].features.count()) {
            list.add(
                index,
                trackWithFeatures.sumOf { it.features.at(index)!! } / trackWithFeatures.size
            )
        }
        return list
    }

    fun findMostSimilar(track: TrackAudioFeatures, otherTracks: List<TrackAudioFeatures>, minMax: List<Pair<Double, Double>>, toTake: Int): List<TrackAudioFeatures> {
        val map = mutableListOf<Pair<TrackAudioFeatures, Double>>()
        for (otherTrack in otherTracks) {
            map.add(Pair(otherTrack, compareTrackFeatures(track, otherTrack, minMax)))
        }
        val result = map.sortedBy { (_, value) -> value }.map { it.first }
        val mostSimilarList = mutableListOf<TrackAudioFeatures>()
        mostSimilarList.addAll(0, result.take(toTake))
        return mostSimilarList.toMutableList()
    }

    fun prepareGenresGroups(availableGenres : GenreSeeds, genresList: List<Array<String>>) : MutableList<GenresGroup>{
        val genreGroups = mutableListOf<GenresGroup>()
        for (genres in genresList){
            if (genres != null) {
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
        }
        return genreGroups
    }

    fun isPartOfGroup(genre: String, genresList: List<String>, genreGroups: MutableList<GenresGroup>) : Boolean{
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
            return Constants.defaultColor
        }
        for (genre in genres) {
            colorArray.add(genreColorMap[genre]!!)
        }
        val finalColor = mixColors(colorArray)
        return String.format("rgba(${finalColor.r}, ${finalColor.g}, ${finalColor.b}, ${finalColor.a})")
    }

    fun removeExtraGenreGroups(genreGroups : MutableList<GenresGroup>){
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

    suspend fun gatherColorsForGenres(artists : List<Artist>) : HashMap<String, MixableColor> {
        val availableGenreSeeds = ApiHelper.getAvailableGenreSeeds() ?: GenreSeeds(arrayOf())
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
                    val randomR = random.nextInt(-4, 5)
                    val randomG = random.nextInt(-4, 5)
                    val randomB = random.nextInt(-4, 5)
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

    fun calculateEdgeWeight(from: Artist?, to: Artist?): Int {
        val firstIndex = max(from?.related_artists?.indexOf(to) ?: 0, 0)
        val secondIndex = max(to?.related_artists?.indexOf(from) ?: 0, 0)
        val first = if (firstIndex != 0) 10 + truncate((20.0 - firstIndex) / 2).toInt() else 0
        val second = if (secondIndex != 0) 10 + truncate((20.0 - secondIndex) / 2).toInt() else 0
        return first + second
    }

    fun calculateEdgeWeightGenres(from: Artist?, to: Artist?): Int {
        return 10
    }

    fun getEdges(recommendedTracks: List<Track>, topArtists: MutableList<Artist>): MutableList<Pair<Artist, Artist>> {
        val edgesList = mutableListOf<Pair<Artist, Artist>>()
        for (recommendedTrack in recommendedTracks) {
            for (relatedArtist in recommendedTrack.artists[0].related_artists) {
                if (topArtists.firstOrNull { it.artistId == relatedArtist.artistId } != null) {
                    edgesList.add(Pair(recommendedTrack.artists[0], relatedArtist))
                }
            }
            for (artist in topArtists) {
                if (artist.related_artists.contains(recommendedTrack.artists[0])) {
                    edgesList.add(Pair(artist, recommendedTrack.artists[0]))
                }
            }
        }
        return edgesList.distinct().toMutableList()
    }

    fun getEdgesTracks(recommendedTracks: List<Track>, topTracks: MutableList<TrackAudioFeatures>): MutableList<Pair<Track, Track>> {
        val edgesList = mutableListOf<Pair<Track, Track>>()
        for (recommendedTrack in recommendedTracks) {
            for (relatedArtist in recommendedTrack.artists[0].related_artists) {
                val relatedTrack = topTracks.firstOrNull { it.track.artists[0].artistId == relatedArtist.artistId }
                if ( relatedTrack != null) {
                    edgesList.add(Pair(recommendedTrack, relatedTrack.track))
                }
            }
            for (topTrack in topTracks) {
                if (topTrack.track.artists[0].related_artists.contains(recommendedTrack.artists[0])) {
                    edgesList.add(Pair(topTrack.track, recommendedTrack))
                }
            }
        }
        return edgesList.distinct().toMutableList()
    }

    fun getGenreEdgesTracks(recommendedTracks: List<Track>, topTracks: MutableList<TrackAudioFeatures>): MutableList<Pair<Track, Track>> {
        val edgesList = mutableListOf<Pair<Track, Track>>()
        for (recommendedTrack in recommendedTracks) {
            for (topTrack in topTracks) {
                if (topTrack.track.artists[0].genres == null || recommendedTrack.artists[0].genres == null) {
                    continue
                }
                val jaccard = (topTrack.track.artists[0].genres!!.intersect(recommendedTrack.artists[0].genres!!.toMutableList()).size.toDouble() / topTrack.track.artists[0].genres!!.union(recommendedTrack.artists[0].genres!!.toMutableList()).size)
                if (jaccard > 0.75) {
                    edgesList.add(Pair(recommendedTrack, topTrack.track))
                }
            }
        }
        return edgesList.distinct().toMutableList()
    }

    fun getGenreEdges(recommendedTracks: List<Track>, topArtists: MutableList<Artist>): MutableList<Pair<Artist, Artist>> {
        val edgesList = mutableListOf<Pair<Artist, Artist>>()
        for (recommendedTrack in recommendedTracks) {
            for (topArtist in topArtists) {
                val jaccard = (topArtist.genres!!.intersect(recommendedTrack.artists[0].genres!!.toMutableList()).size.toDouble() / topArtist.genres!!.union(recommendedTrack.artists[0].genres!!.toMutableList()).size)
                if (jaccard > 0.25) {
                    edgesList.add(Pair(recommendedTrack.artists[0], topArtist))
                }
            }
        }
        return edgesList.distinct().toMutableList()
    }

    fun getFeaturesEdges(recommendedTracks: List<TrackAudioFeatures>, topTracks: MutableList<TrackAudioFeatures>): MutableList<GraphFeaturesLink> {
        val edgesList = mutableListOf<GraphFeaturesLink>()
        for (recommendedTrack in recommendedTracks) {
            for (topTrack in topTracks) {
                val featuresList = mutableListOf<Pair<AudioFeatureType, Double>>()
                for (index in 0 until 2){
                    val similarity = featuresSimilar(recommendedTrack.features.at(index)!!, topTrack.features.at(index)!!)
                    if (similarity > 0.90)  {
                        featuresList.add(Pair(AudioFeatureType.values()[index], similarity))
                    }
                }
                if (featuresList.size > 1) {
                    edgesList.add(
                        GraphFeaturesLink(
                            Pair(recommendedTrack.track, topTrack.track),
                            featuresList
                        )
                    )
                }
            }
        }
        return edgesList
    }

    fun featuresSimilar(value1: Double, value2:Double): Double {
        return if (abs(value1) > abs(value2)) {
            abs(value2) / abs(value1)
        } else {
            abs(value1) / abs(value2)
        }
    }

    fun getTopTracksEdges(tracks: List<Track>): MutableList<Pair<Track, Track>> {
        val edgesList = mutableListOf<Pair<Track, Track>>()
        for (track in tracks) {
            for (relatedArtist in track.artists[0].related_artists) {
                val relatedTrack = tracks.firstOrNull { it.artists[0].artistId == relatedArtist.artistId }
                if (relatedTrack != null) {
                    edgesList.add(Pair(track, relatedTrack))
                }
            }
        }
        return edgesList
    }

    fun getTopTracksNodes(genreColorMap: HashMap<String, MixableColor>, tracks: List<Track>): ArrayList<D3Node> {
        val relationDataNodes = ArrayList<D3Node>()
        for (track in tracks) {
            relationDataNodes.add(
                D3Node(
                    track.trackName.replace('"', '&'),
                    NodeType.RECOMMENDED.value,
                    max(track.popularity / Constants.recommendGraphPopularityFactor, 8),
                    getArtistColor(track.artists[0].genres, genreColorMap)
                )
            )
        }
        return relationDataNodes
    }

    fun getTopTracksLinks(edgesList: MutableList<Pair<Track, Track>>): ArrayList<D3Link>{
        val relationDataLinks = ArrayList<D3Link>()
        for (pair in edgesList) {
            relationDataLinks.add(
                D3Link(
                    pair.first.trackName.replace('"', '&'),
                    pair.second.trackName.replace('"', '&'),
                    calculateEdgeWeight(pair.first.artists[0], pair.second.artists[0]),
                    Constants.defaultColor,
                    LinkType.RELATED
                )
            )
        }
        return relationDataLinks
    }

    fun getTopArtistsEdges(artists: List<Artist>): MutableList<Pair<Artist, Artist>> {
        val edgesList = mutableListOf<Pair<Artist, Artist>>()
        for (artist in artists) {
            for (relatedArtist in artist.related_artists) {
                if (artists.firstOrNull { it.artistId == relatedArtist.artistId } != null) {
                    edgesList.add(Pair(artist, relatedArtist))
                }
            }
        }
        return edgesList
    }

    fun getTopArtistsGenresEdges(artists: List<Artist>): MutableList<Pair<Artist, Artist>> {
        val edgesList = mutableListOf<Pair<Artist, Artist>>()
        for (i in artists.indices) {
            for (j in i until artists.size) {
                if (artists[i].genres == null || artists[j].genres == null) {
                    continue
                }
                val jaccard = (artists[i].genres!!.intersect(artists[j].genres!!.toMutableList()).size.toDouble() / artists[i].genres!!.union(artists[j].genres!!.toMutableList()).size)
                if (jaccard > 0.25) {
                    edgesList.add(Pair(artists[i], artists[j]))
                }
            }
        }
        return edgesList.distinct().toMutableList()
    }




    fun getTopArtistNodes(genreColorMap: HashMap<String, MixableColor>, artists: List<Artist>): ArrayList<D3Node> {
        val relationDataNodes = ArrayList<D3Node>()
        for (artist in artists) {
            relationDataNodes.add(
                D3Node(
                    artist.artistName,
                    NodeType.RECOMMENDED.value,
                    artist.artistPopularity!! / Constants.recommendGraphPopularityFactor,
                    getArtistColor(artist.genres, genreColorMap)
                )
            )
        }
        return relationDataNodes
    }


    fun getNodes(genreColorMap: HashMap<String, MixableColor>, nodeArtists: List<Artist>, topArtists: MutableList<Artist>, recommendedTracks: List<Track>): ArrayList<D3Node> {
        val relationDataNodes = ArrayList<D3Node>()
        for (artist in nodeArtists) {
            relationDataNodes.add(
                D3Node(
                    artist.artistName,
                    if (topArtists.contains(artist) && recommendedTracks.firstOrNull { it.artists[0].artistId == artist.artistId } != null) {
                        NodeType.COMBINED.value
                    } else if (topArtists.contains(artist)) {
                        NodeType.TOP.value
                    } else NodeType.RECOMMENDED.value,
                    artist.artistPopularity!! / Constants.recommendGraphPopularityFactor,
                    getArtistColor(artist.genres, genreColorMap)
                )
            )
        }
        return relationDataNodes
    }

    fun getNodesSizeBasedOnEdges(
        genreColorMap: HashMap<String, MixableColor>,
        nodeArtists: List<Artist>,
        topArtists: MutableList<Artist>,
        recommendedTracks: List<Track>,
        edgesList: MutableList<Pair<Artist, Artist>>
    ): ArrayList<D3Node> {
        val relationDataNodes = ArrayList<D3Node>()
        for (artist in nodeArtists) {
            relationDataNodes.add(
                D3Node(
                    artist.artistName,
                    if (topArtists.contains(artist) && recommendedTracks.firstOrNull { it.artists[0].artistId == artist.artistId } != null) {
                        NodeType.COMBINED.value
                    } else if (topArtists.contains(artist)) {
                        NodeType.TOP.value
                    } else NodeType.RECOMMENDED.value,
                    edgesList.count { it.first.artistId == artist.artistId || it.second.artistId == artist.artistId} * 2 + 30,
                    getArtistColor(artist.genres, genreColorMap)
                )
            )
        }
        return relationDataNodes
    }

    fun getNodesSizeBasedOnEdgesArtists(
        genreColorMap: HashMap<String, MixableColor>,
        nodeArtists: List<Artist>,
        edgesList: MutableList<Pair<Artist, Artist>>
    ): ArrayList<D3Node> {
        val relationDataNodes = ArrayList<D3Node>()
        for (artist in nodeArtists) {
            relationDataNodes.add(
                D3Node(
                    artist.artistName,
                    NodeType.RECOMMENDED.value,
                    edgesList.count { it.first.artistId == artist.artistId || it.second.artistId == artist.artistId} * 2 + 30,
                    getArtistColor(artist.genres, genreColorMap)
                )
            )
        }
        return relationDataNodes
    }

    fun prepareNodesTracks(
        genreColorMap: HashMap<String, MixableColor>,
        nodeTracks: List<Track>,
        topTracks: MutableList<Track>,
        recommendedTracks: List<Track>,
        edgesList: MutableList<Pair<Track, Track>>
    ): ArrayList<D3Node> {
        val relationDataNodes = ArrayList<D3Node>()
        for (track in nodeTracks) {
            relationDataNodes.add(
                D3Node(
                    track.trackName.replace('"', '&'),
                    if (topTracks.map{it.artists[0]}.contains(track.artists[0]) && recommendedTracks.firstOrNull { it.artists[0].artistId == track.artists[0].artistId } != null) {
                        NodeType.COMBINED.value
                    } else if (topTracks.contains(track)) {
                        NodeType.TOP.value
                    } else NodeType.RECOMMENDED.value,
                    edgesList.count { it.first.trackId == track.trackId|| it.second.trackId == track.trackId} * 2 + 30,
                    getArtistColor(track.artists[0].genres, genreColorMap)
                )
            )
        }
        return relationDataNodes
    }

    fun getLinksGenres(edgesList: MutableList<Pair<Artist, Artist>>, genreColorMap: HashMap<String, MixableColor>): ArrayList<D3Link>{
        val relationDataLinks = ArrayList<D3Link>()
        for (pair in edgesList) {
            relationDataLinks.add(
                D3Link(
                    pair.first.getName(),
                    pair.second.getName(),
                    10,
                    getArtistColor(pair.first.genres!!.intersect(pair.second.genres!!.toMutableList()).toTypedArray(), genreColorMap),
                    LinkType.GENRE
                )
            )
        }
        return relationDataLinks
    }

    fun getLinksGenresTracks(edgesList: MutableList<Pair<Track, Track>>, genreColorMap: HashMap<String, MixableColor>): ArrayList<D3Link>{
        val relationDataLinks = ArrayList<D3Link>()
        for (pair in edgesList) {
            relationDataLinks.add(
                D3Link(
                    pair.first.trackName.replace('"', '&'),
                    pair.second.trackName.replace('"', '&'),
                    10,
                    getArtistColor(pair.first.artists[0].genres!!.intersect(pair.second.artists[0].genres!!.toMutableList()).toTypedArray(), genreColorMap),
                LinkType.GENRE
            )
            )
        }
        return relationDataLinks
    }

    fun getLinksFeatures(edgesList: MutableList<Pair<Track, Track>>): ArrayList<D3Link>{
        val relationDataLinks = ArrayList<D3Link>()
        for (pair in edgesList) {
            relationDataLinks.add(
                D3Link(
                    pair.first.trackName.replace('"', '&'),
                    pair.second.trackName.replace('"', '&'),
                    10,
                    Constants.defaultColor,
                    LinkType.FEATURE
                )
            )
        }
        return relationDataLinks
    }

    fun getLinks(edgesList: MutableList<Pair<Artist, Artist>>): ArrayList<D3Link>{
        val relationDataLinks = ArrayList<D3Link>()
        for (pair in edgesList) {
            relationDataLinks.add(
                D3Link(
                    pair.first.artistName,
                    pair.second.artistName,
                    calculateEdgeWeight(pair.first, pair.second),
                    Constants.defaultColor,
                    LinkType.RELATED
                )
            )
        }
        return relationDataLinks
    }

    fun getLinksTracks(edgesList: MutableList<Pair<Track, Track>>): ArrayList<D3Link>{
        val relationDataLinks = ArrayList<D3Link>()
        for (pair in edgesList) {
            relationDataLinks.add(
                D3Link(
                    pair.first.trackName.replace('"', '&'),
                    pair.second.trackName.replace('"', '&'),
                    calculateEdgeWeight(pair.first.artists[0], pair.second.artists[0]),
                    Constants.defaultColor,
                    LinkType.RELATED
                )
            )
        }
        return relationDataLinks
    }

    fun <T> removeDuplicateEdges(edgesList: MutableList<Pair<T, T>>) : MutableList<Pair<T, T>> {
        val uniqueList = mutableListOf<Pair<T, T>>()
        for (pair in edgesList) {
            if (uniqueList.contains(Pair(pair.second, pair.first))){
                continue
            } else {
                uniqueList.add(pair)
            }
        }
        return uniqueList
    }

    fun getGenreColorList(artist : Artist, genreColorMap: HashMap<String, MixableColor>): List<GenreColor>?{
        return artist.genres?.map {
            GenreColor(
                it,
                arrayListOf(
                    genreColorMap[it]!!.a.toFloat(),
                    genreColorMap[it]!!.r.toFloat(),
                    genreColorMap[it]!!.g.toFloat(),
                    genreColorMap[it]!!.b.toFloat(),
                )
            )
        }
    }

    fun getGenreColorList(colors : List<String>, genreColorMap: HashMap<String, MixableColor>): List<GenreColor> {
        return colors.map {
            GenreColor(
                it,
                arrayListOf(
                    genreColorMap[it]!!.a.toFloat(),
                    genreColorMap[it]!!.r.toFloat(),
                    genreColorMap[it]!!.g.toFloat(),
                    genreColorMap[it]!!.b.toFloat(),
                )
            )
        }
    }

    fun getGenreColorList(color: String, genreColorMap: HashMap<String, MixableColor>): ArrayList<Float> {
        return arrayListOf(
            genreColorMap[color]!!.a.toFloat(),
            genreColorMap[color]!!.r.toFloat(),
            genreColorMap[color]!!.g.toFloat(),
            genreColorMap[color]!!.b.toFloat()
        )
    }



    fun prepareD3RelationsDistance(tracks: MutableList<TrackAudioFeatures>): Triple<ArrayList<D3Node>, ArrayList<D3LinkDistance>, List<BundleTrackFeatureItem>> {
        val relationDataNodes = ArrayList<D3Node>()
        val relationDataLinks = ArrayList<D3LinkDistance>()
        val minMaxFeatures = getMinMaxFeatures(tracks)
        val allGraphNodes = mutableListOf<BundleTrackFeatureItem>()
        for (recommendedTrack in tracks) {
            relationDataNodes.add(D3Node(
                recommendedTrack.track.trackName,
                2,
                recommendedTrack.track.popularity / Constants.recommendGraphPopularityFactor + 10,
                Constants.nodeColorTrack
            ))
            allGraphNodes.add(BundleTrackFeatureItem(
                recommendedTrack.track,
                recommendedTrack.features.asList().map { pair -> Pair(
                    pair.first,
                    normalize(
                        pair.second!!,
                        minMaxFeatures[recommendedTrack.features.getIndex(pair.first)].first,
                        minMaxFeatures[recommendedTrack.features.getIndex(pair.first)].second)
                )},
                BundleItemType.TRACK,
                null
            ))
            for (index in 0 until recommendedTrack.features.count()) {
                relationDataLinks.add(D3LinkDistance(
                    recommendedTrack.track.trackName,
                    recommendedTrack.features.getName(index),
                    0,
                    (100 - (normalize(
                        recommendedTrack.features.at(index)!!,
                        tracks.minOf { it.features.at(index)!! },
                        tracks.maxOf { it.features.at(index)!! }
                    ) *100).roundToInt())*10
                ))
            }
        }
        for (index in 0 until tracks[0].features.count()){
            relationDataNodes.add(D3Node(
                tracks[0].features.getName(index),
                1,
                Constants.featureNodeRadius,
                Constants.nodeColorFeature
            ))
            //Log.e("feature", "${tracks[0].features.getName(index)} --> ${tracks.sortedByDescending { it.features.at(index) }.map { "${it.track.trackName} -- ${it.features.at(index)} \n"}}")
            allGraphNodes.add(BundleTrackFeatureItem(null, listOf(), BundleItemType.FEATURE, AudioFeatureType.values()[index]))
        }
        return Triple(relationDataNodes, relationDataLinks, allGraphNodes)
    }

    fun showDetailInfo(selectedArtist: Artist, tracks: MutableList<Track>, genreColorMap: HashMap<String, MixableColor>): List<GenreColor>? {
        val selectedTrack = tracks.firstOrNull { it.artists[0].artistId == selectedArtist.artistId }
        val artistName = selectedArtist.artistName
        val imageUrl = if (selectedArtist.images!!.isNotEmpty()) selectedArtist.images!![0].url else ""
        return Helper.getGenreColorList(selectedArtist, genreColorMap)
    }

    fun showBundleDetailInfo(nodeIndices: String, currentIndex: String, nodesArtists: List<Artist>, nodesTracks: List<Track>, recommendedTracks: List<Track>, genreColorMap: HashMap<String, MixableColor>): MutableList<BundleGraphItem> {
        val bundleItems = mutableListOf<BundleGraphItem>()
        if (nodesArtists.isNotEmpty()) {
            val artistIds = nodeIndices.split(',').map { artist -> nodesArtists[artist.toInt()] }
                .toMutableList()
            artistIds.add(nodesArtists[currentIndex.toInt()])
            for (artist in artistIds) {
                val recTrack =
                    recommendedTracks.firstOrNull { it.artists[0].artistId == artist!!.artistId }
                val genresList = getGenreColorList(artist!!, genreColorMap)
                bundleItems.add(
                    BundleGraphItem(
                        recTrack,
                        artist,
                        genresList,
                        null,
                        if (recTrack != null) BundleItemType.TRACK else BundleItemType.ARTIST
                    )
                )
            }
        } else {
            val tracks = nodeIndices.split(',').map { track -> nodesTracks[track.toInt()] }
                .toMutableList()
            tracks.add(nodesTracks[currentIndex.toInt()])
            for (track in tracks) {
                val genresList = getGenreColorList(track.artists[0], genreColorMap)
                bundleItems.add(
                    BundleGraphItem(
                        track,
                        track.artists[0],
                        genresList,
                        null,
                        BundleItemType.TRACK
                    )
                )
            }
        }
        return bundleItems
    }

    fun showLineDetailInfo(nodeIndices: String?, nodes: List<Artist>, nodesTracks: List<Track>): LineDetailInfo {
        val messageItems = nodeIndices!!.split(",")
        val sourceArtist: Artist
        val targetArtist: Artist
        if (nodes.isNotEmpty()) {
            sourceArtist = nodes[messageItems[0].toInt()]
            targetArtist = nodes[messageItems[1].toInt()]
        } else {
            sourceArtist = nodesTracks[messageItems[0].toInt()].artists[0]
            targetArtist = nodesTracks[messageItems[1].toInt()].artists[0]
        }
        val sourceToTarget = sourceArtist.related_artists?.indexOf(targetArtist) ?: -1
        val targetToSource = targetArtist.related_artists?.indexOf(sourceArtist) ?: -1
        return LineDetailInfo(
            sourceArtist,
            targetArtist,
            if (sourceToTarget != -1) sourceToTarget + 1 else null,
            if (targetToSource != -1) targetToSource + 1 else null
        )
    }

    fun showLineDetailGenreInfo(nodeIndices: String?, nodes: List<Artist>, nodesTracks: List<Track>): LineDetailGenreInfo {
        val messageItems = nodeIndices!!.split(",")
        val sourceArtist: Artist
        val targetArtist: Artist
        if (nodes.isNotEmpty()) {
            sourceArtist = nodes[messageItems[0].toInt()]
            targetArtist = nodes[messageItems[1].toInt()]
        } else {
            sourceArtist = nodesTracks[messageItems[0].toInt()].artists[0]
            targetArtist = nodesTracks[messageItems[1].toInt()].artists[0]
        }
        val intersection = sourceArtist.genres!!.intersect(targetArtist.genres!!.toMutableList()).toList()
        return LineDetailGenreInfo(
            sourceArtist,
            targetArtist,
            intersection
        )
    }

    fun showLineDetailFeatureInfo(nodeIndices: String?, nodesTracks: List<TrackAudioFeatures>): LineDetailFeatureInfo {
        val messageItems = nodeIndices!!.split(",")
        val sourceTrack = nodesTracks[messageItems[0].toInt()]
        val targetTrack = nodesTracks[messageItems[1].toInt()]
        val featuresList = mutableListOf<Pair<AudioFeatureType, Double>>()
        for (index in 0 until AudioFeatureType.values().size){
            val similarity = featuresSimilar(sourceTrack.features.at(index)!!, targetTrack.features.at(index)!!)
            if (similarity > 0.90)  {
                featuresList.add(Pair(AudioFeatureType.values()[index], similarity))
            }
        }
        return LineDetailFeatureInfo(
            sourceTrack,
            targetTrack,
            featuresList
        )
    }

    fun showBundleLineInfoTracks(nodeIndices: String?, nodesTracks: List<TrackAudioFeatures>, genreColorMap: HashMap<String, MixableColor>): LineDetailBundleInfo {
        val messageItems = nodeIndices!!.split(",")
        var sourceArtist: Artist
        var targetArtist: Artist
        val items = mutableListOf<BundleLineInfo>()
        for (line in messageItems.drop(2).dropLast(1)){
            val lineElements = line.split("-")
            sourceArtist = nodesTracks[lineElements[0].toInt()].track.artists[0]
            targetArtist = nodesTracks[lineElements[1].toInt()].track.artists[0]
            val sourceTrack = nodesTracks[lineElements[0].toInt()]
            val targetTrack = nodesTracks[lineElements[1].toInt()]
            when (lineElements[2]) {
                "RELATED" -> {
                    val sourceToTarget = sourceArtist.related_artists.indexOf(targetArtist)
                    val targetToSource = targetArtist.related_artists.indexOf(sourceArtist)
                    items.add(BundleLineInfo(sourceArtist = sourceArtist, targetArtist = targetArtist, sourceTrack = sourceTrack, targetTrack = targetTrack, sToT = sourceToTarget, tToS = targetToSource, linkType = LinkType.RELATED))
                }
                "GENRE" -> {
                    val intersection = sourceArtist.genres!!.intersect(targetArtist.genres!!.toMutableList().toSet()).toList()
                    for (genre in intersection){
                        items.add(BundleLineInfo(sourceArtist = sourceArtist, targetArtist = targetArtist, sourceTrack = sourceTrack, targetTrack = targetTrack, genres = genre, genreColor = getGenreColorList(genre, genreColorMap), linkType = LinkType.GENRE))
                    }
                }
                "FEATURE" -> {
                    val featuresList = mutableListOf<Pair<AudioFeatureType, Double>>()
                    for (index in 0 until AudioFeatureType.values().size){
                        val similarity = featuresSimilar(sourceTrack.features.at(index)!!, targetTrack.features.at(index)!!)
                        if (similarity > 0.90)  {
                            featuresList.add(Pair(AudioFeatureType.values()[index], similarity))
                        }
                    }
                    for (feature in featuresList){
                        items.add(BundleLineInfo(sourceArtist = sourceArtist, targetArtist = targetArtist, sourceTrack = sourceTrack, targetTrack = targetTrack, features = feature, linkType = LinkType.FEATURE))
                    }
                }
            }
        }
        return LineDetailBundleInfo(
            items
        )
    }

    fun showBundleLineInfoArtists(nodeIndices: String?, artists: List<Artist>, genreColorMap: HashMap<String, MixableColor>): LineDetailBundleInfo {
        val messageItems = nodeIndices!!.split(",")
        var sourceArtist: Artist
        var targetArtist: Artist
        val items = mutableListOf<BundleLineInfo>()
        for (line in messageItems.drop(2).dropLast(1)){
            val lineElements = line.split("-")
            sourceArtist = artists[lineElements[0].toInt()]
            targetArtist = artists[lineElements[1].toInt()]
            when (lineElements[2]) {
                "RELATED" -> {
                    val sourceToTarget = sourceArtist.related_artists.indexOf(targetArtist)
                    val targetToSource = targetArtist.related_artists.indexOf(sourceArtist)
                    items.add(BundleLineInfo(sourceArtist = sourceArtist, targetArtist = targetArtist, sToT = sourceToTarget, tToS = targetToSource,  linkType = LinkType.RELATED))
                }
                "GENRE" -> {
                    val intersection = sourceArtist.genres!!.intersect(targetArtist.genres!!.toMutableList().toSet()).toList()
                    for (genre in intersection){
                        items.add(BundleLineInfo(sourceArtist = sourceArtist, targetArtist = targetArtist, genres = genre, genreColor = getGenreColorList(genre, genreColorMap), linkType = LinkType.GENRE))
                    }
                }
            }
        }
        return LineDetailBundleInfo(
            items
        )
    }

    suspend fun prepareGraphTopArtists(settings: GraphSettings, topArtists: MutableList<Artist>): ForceGraph {
        val edgesList = mutableListOf<Pair<Artist, Artist>>()
        val links = arrayListOf<D3Link>()
        var edgesListGenres = mutableListOf<Pair<Artist,Artist>>()
        if (settings.relatedFlag) {
            val edgesListRelated = getTopArtistsEdges(topArtists)
            edgesList.addAll(edgesListRelated)
            links.addAll(getLinks(edgesListRelated))
        }
        if (settings.genresFlag) {
            edgesListGenres = getTopArtistsGenresEdges(topArtists)
            edgesList.addAll(edgesListGenres)
        }
        val nodeArtists =
            ((edgesList.map { it.first } + edgesList.map { it.second }) + topArtists).distinct()
        val genreColorMap = gatherColorsForGenres(nodeArtists)
        val nodes = getNodesSizeBasedOnEdgesArtists(
            genreColorMap,
            nodeArtists,
            edgesList
        )
        if (settings.genresFlag) {
            links.addAll(getLinksGenres(edgesListGenres, genreColorMap))
        }
        return ForceGraph(links, nodes, genreColorMap, nodeArtists, listOf(), BundleItemType.ARTIST)
    }

    suspend fun prepareGraphArtists(settings: GraphSettings, recommendedTracks: List<Track>, topArtists: MutableList<Artist>): ForceGraph {
        val edgesList = mutableListOf<Pair<Artist, Artist>>()
        val links = arrayListOf<D3Link>()
        var edgesListGenres = mutableListOf<Pair<Artist,Artist>>()
        if (settings.relatedFlag) {
            val edgesListRelated =  getEdges(recommendedTracks, topArtists)
            edgesList.addAll(edgesListRelated)
            links.addAll(getLinks(edgesListRelated))
        }
        if (settings.genresFlag) {
            edgesListGenres = getGenreEdges(recommendedTracks, topArtists)
            edgesList.addAll(edgesListGenres)
        }
        val nodeArtists =
            ((edgesList.map { it.first } + edgesList.map { it.second }) + recommendedTracks.map { it.artists[0] }).distinct()
        val genreColorMap = gatherColorsForGenres(nodeArtists)
        val nodes = getNodesSizeBasedOnEdges(
            genreColorMap,
            nodeArtists,
            topArtists,
            recommendedTracks,
            edgesList
        )
        if (settings.genresFlag) {
            links.addAll(getLinksGenres(edgesListGenres, genreColorMap))
        }
        return ForceGraph(links, nodes, genreColorMap, nodeArtists, listOf(), BundleItemType.ARTIST)
    }

    suspend fun prepareGraphTracks(settings: GraphSettings, recommendedTracksFeatures: List<TrackAudioFeatures>, topTracks: MutableList<TrackAudioFeatures>): ForceGraph {
        val edgesList = mutableListOf<Pair<Track, Track>>()
        val links = arrayListOf<D3Link>()
        var edgesListGenres = mutableListOf<Pair<Track,Track>>()
        if (settings.relatedFlag) {
            val edgesListRelated = getEdgesTracks(recommendedTracksFeatures.map { it.track }, topTracks.toMutableList())
            edgesList.addAll(edgesListRelated)
            links.addAll(getLinksTracks(edgesListRelated))
        }
        if (settings.genresFlag) {
            edgesListGenres = getGenreEdgesTracks(recommendedTracksFeatures.map { it.track }, topTracks.toMutableList())
            edgesList.addAll(edgesListGenres)

        }
        if (settings.featuresFlag) {
            val edgesListFeatures = getFeaturesEdges(recommendedTracksFeatures, topTracks.toMutableList())
            edgesList.addAll(edgesListFeatures.map { it.tracks })
            links.addAll(getLinksFeatures(edgesListFeatures.map { it.tracks }.toMutableList()))
        }
        val nodeTracks =
            ((edgesList.map { it.first } + edgesList.map { it.second}) + recommendedTracksFeatures.map { it.track }).distinct()
        val genreColorMap = gatherColorsForGenres(nodeTracks.map { it.artists[0] })
        val nodes = prepareNodesTracks(
            genreColorMap,
            nodeTracks,
            topTracks.map { it.track }.toMutableList(),
            recommendedTracksFeatures.map { it.track },
            edgesList
        )
        if (settings.genresFlag) {
            links.addAll(getLinksGenresTracks(edgesListGenres, genreColorMap))
        }
        return ForceGraph(links, nodes, genreColorMap, listOf(), nodeTracks, BundleItemType.TRACK)
    }
}