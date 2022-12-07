package com.example.spotifyexplained.general

import android.content.Context
import android.content.res.Configuration
import com.example.spotifyexplained.R
import com.example.spotifyexplained.model.*
import com.example.spotifyexplained.model.enums.AudioFeatureType
import com.example.spotifyexplained.model.enums.BundleItemType
import com.example.spotifyexplained.model.enums.LinkType
import com.example.spotifyexplained.model.enums.NodeType
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
        return 1 - sum/9
    }

    fun getSimilarity(recomTrack: TrackAudioFeatures, targetTrack: List<Double>, minMax: List<Pair<Double, Double>>) : Double {
        val sum = compareTrackFeatures(recomTrack, targetTrack, minMax)
        return 1 - sum/9
    }

    fun normalize(rawValue: Double, min: Double, max: Double) : Double {
        return ((rawValue - min) / (max - min))
    }

    fun mixColors(colors: ArrayList<MixableColor>): MixableColor {
        return MixableColor(
            colors.map { it.r }.average().toInt(),
            colors.map { it.g }.average().toInt(),
            colors.map { it.b }.average().toInt(),
            1.0
        )
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

    fun getAverageFeatures(trackWithFeatures: List<TrackAudioFeatures>): MutableList<Double> {
        val list = mutableListOf<Double>()
        for (index in 0 until trackWithFeatures[0].features.count()) {
            list.add(
                index,
                trackWithFeatures.sumOf { it.features.at(index)!! } / trackWithFeatures.size
            )
        }
        return list
    }

    fun calculateEdgeWeight(from: Artist?, to: Artist?): Int {
        val firstIndex = max(from?.related_artists?.indexOf(to) ?: 0, 0)
        val secondIndex = max(to?.related_artists?.indexOf(from) ?: 0, 0)
        val first = if (firstIndex != 0) 10 + truncate((20.0 - firstIndex) / 2).toInt() else 0
        val second = if (secondIndex != 0) 10 + truncate((20.0 - secondIndex) / 2).toInt() else 0
        return first + second
    }

    fun getEdges(recommendedTracks: List<Track>, topArtists: List<Artist>): MutableList<Pair<Artist, Artist>> {
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
                if (topTrack.track.artists[0].genres == null || recommendedTrack.artists[0].genres == null || topTrack.track.trackId == recommendedTrack.trackId) {
                    continue
                }
                val jaccard = (topTrack.track.artists[0].genres!!.intersect(recommendedTrack.artists[0].genres!!.toMutableList()).size.toDouble() / topTrack.track.artists[0].genres!!.union(recommendedTrack.artists[0].genres!!.toMutableList()).size)
                if (jaccard > Config.jaccardTracks) {
                    if (edgesList.find { it.first.trackId == topTrack.track.trackId && it.second.trackId == recommendedTrack.trackId } == null) {
                        edgesList.add(Pair(recommendedTrack, topTrack.track))
                    }
                }
            }
        }
        return edgesList.distinct().toMutableList()
    }

    fun getGenreEdges(recommendedTracks: List<Track>, topArtists: List<Artist>): MutableList<Pair<Artist, Artist>> {
        val edgesList = mutableListOf<Pair<Artist, Artist>>()
        for (recommendedTrack in recommendedTracks) {
            for (topArtist in topArtists) {
                val jaccard = (topArtist.genres!!.intersect(recommendedTrack.artists[0].genres!!.toMutableList()).size.toDouble() / topArtist.genres!!.union(recommendedTrack.artists[0].genres!!.toMutableList()).size)
                if (jaccard > Config.jaccardArtists) {
                    edgesList.add(Pair(recommendedTrack.artists[0], topArtist))
                }
            }
        }
        return edgesList.distinct().toMutableList()
    }

    fun getFeaturesEdges(recommendedTracks: List<TrackAudioFeatures>?, topTracks: MutableList<TrackAudioFeatures>?): MutableList<GraphFeaturesLink> {
        val edgesList = mutableListOf<GraphFeaturesLink>()
        for (recommendedTrack in recommendedTracks ?: listOf()) {
            for (topTrack in topTracks ?: mutableListOf()) {
                val featuresList = mutableListOf<Pair<AudioFeatureType, Double>>()
                for (index in 0 until (recommendedTrack.features.count())){
                    val similarity = featuresSimilar(recommendedTrack.features.at(index)!!, topTrack.features.at(index)!!)
                    if (similarity > Config.featuresSimilarityThreshold)  {
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
            if (abs(value1) > 0) {
                abs(value2) / abs(value1)
            } else {
                abs(value2)
            }
        } else {
            if (abs(value2) > 0) {
                abs(value1) / abs(value2)
            } else {
               abs(value1)
            }
        }
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
                if (jaccard > Config.jaccardTopTracks) {
                    edgesList.add(Pair(artists[i], artists[j]))
                }
            }
        }
        return edgesList.distinct().toMutableList()
    }


    fun getNodesSizeBasedOnEdges(
        genreColorMap: HashMap<String, MixableColor>,
        nodeArtists: List<Artist>,
        topArtists: List<Artist>,
        recommendedTracks: List<Track>,
        edgesList: MutableList<Pair<Artist, Artist>>
    ): ArrayList<D3ForceNode> {
        val relationDataNodes = ArrayList<D3ForceNode>()
        for (artist in nodeArtists) {
            relationDataNodes.add(
                D3ForceNode(
                    artist.artistName,
                    if (topArtists.contains(artist) && recommendedTracks.firstOrNull { it.artists[0].artistId == artist.artistId } != null) {
                        NodeType.COMBINED.value
                    } else if (topArtists.contains(artist)) {
                        NodeType.TOP.value
                    } else NodeType.RECOMMENDED.value,
                    edgesList.count { it.first.artistId == artist.artistId || it.second.artistId == artist.artistId} * Config.nodeDegreeSizeFactor + Config.nodeDegreeSizeConstant,
                    ColorHelper.getArtistColor(artist.genres, genreColorMap)
                )
            )
        }
        return relationDataNodes
    }

    fun getNodesSizeBasedOnEdgesArtists(
        genreColorMap: HashMap<String, MixableColor>,
        nodeArtists: List<Artist>,
        edgesList: MutableList<Pair<Artist, Artist>>
    ): ArrayList<D3ForceNode> {
        val relationDataNodes = ArrayList<D3ForceNode>()
        for (artist in nodeArtists) {
            relationDataNodes.add(
                D3ForceNode(
                    artist.artistName,
                    NodeType.RECOMMENDED.value,
                    edgesList.count { it.first.artistId == artist.artistId || it.second.artistId == artist.artistId} * Config.nodeDegreeSizeFactor + Config.nodeDegreeSizeConstant,
                    ColorHelper.getArtistColor(artist.genres, genreColorMap)
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
    ): ArrayList<D3ForceNode> {
        val relationDataNodes = ArrayList<D3ForceNode>()
        for (track in nodeTracks) {
            relationDataNodes.add(
                D3ForceNode(
                    track.trackName.replace('"', '&'),
                    if (topTracks.map { it.artists[0] }.contains(track.artists[0]) && recommendedTracks.firstOrNull { it.artists[0].artistId == track.artists[0].artistId } != null) {
                        NodeType.COMBINED.value
                    } else if (topTracks.contains(track)) {
                        NodeType.TOP.value
                    } else NodeType.RECOMMENDED.value,
                    edgesList.count { it.first.trackId == track.trackId || it.second.trackId == track.trackId } * Config.nodeDegreeSizeFactor + Config.nodeDegreeSizeConstant,
                    ColorHelper.getArtistColor(track.artists[0].genres, genreColorMap)
                )
            )
        }
        return relationDataNodes
    }

    fun getLinksGenres(edgesList: MutableList<Pair<Artist, Artist>>, genreColorMap: HashMap<String, MixableColor>): ArrayList<D3ForceLink>{
        val relationDataLinks = ArrayList<D3ForceLink>()
        for (pair in edgesList) {
            relationDataLinks.add(
                D3ForceLink(
                    pair.first.artistName,
                    pair.second.artistName,
                    Config.baseLineWidth,
                    ColorHelper.getArtistColor(pair.first.genres!!.intersect(pair.second.genres!!.toMutableList()).toTypedArray(), genreColorMap),
                    LinkType.GENRE
                )
            )
        }
        return relationDataLinks
    }

    fun getLinksGenresTracks(edgesList: MutableList<Pair<Track, Track>>, genreColorMap: HashMap<String, MixableColor>): ArrayList<D3ForceLink>{
        val relationDataLinks = ArrayList<D3ForceLink>()
        for (pair in edgesList) {
            relationDataLinks.add(
                D3ForceLink(
                    pair.first.trackName.replace('"', '&'),
                    pair.second.trackName.replace('"', '&'),
                    Config.baseLineWidth,
                    ColorHelper.getArtistColor(pair.first.artists[0].genres!!.intersect(pair.second.artists[0].genres!!.toMutableList()).toTypedArray(), genreColorMap),
                LinkType.GENRE
            )
            )
        }
        return relationDataLinks
    }

    fun getLinksFeatures(edgesList: MutableList<Pair<Track, Track>>): ArrayList<D3ForceLink>{
        val relationDataLinks = ArrayList<D3ForceLink>()
        for (pair in edgesList) {
            relationDataLinks.add(
                D3ForceLink(
                    pair.first.trackName.replace('"', '&'),
                    pair.second.trackName.replace('"', '&'),
                    Config.baseLineWidth,
                    Config.defaultColor,
                    LinkType.FEATURE
                )
            )
        }
        return relationDataLinks
    }

    fun getLinks(edgesList: MutableList<Pair<Artist, Artist>>): ArrayList<D3ForceLink>{
        val relationDataLinks = ArrayList<D3ForceLink>()
        for (pair in edgesList) {
            relationDataLinks.add(
                D3ForceLink(
                    pair.first.artistName,
                    pair.second.artistName,
                    calculateEdgeWeight(pair.first, pair.second),
                    Config.defaultColor,
                    LinkType.RELATED
                )
            )
        }
        return relationDataLinks
    }

    fun getLinksTracks(edgesList: MutableList<Pair<Track, Track>>): ArrayList<D3ForceLink>{
        val relationDataLinks = ArrayList<D3ForceLink>()
        for (pair in edgesList) {
            relationDataLinks.add(
                D3ForceLink(
                    pair.first.trackName.replace('"', '&'),
                    pair.second.trackName.replace('"', '&'),
                    calculateEdgeWeight(pair.first.artists[0], pair.second.artists[0]),
                    Config.defaultColor,
                    LinkType.RELATED
                )
            )
        }
        return relationDataLinks
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

    fun prepareD3RelationsDistance(tracks: MutableList<TrackAudioFeatures>): Triple<ArrayList<D3ForceNode>, ArrayList<D3ForceLinkDistance>, List<BundleTrackFeatureItem>> {
        val relationDataNodes = ArrayList<D3ForceNode>()
        val relationDataLinks = ArrayList<D3ForceLinkDistance>()
        val minMaxFeatures = getMinMaxFeatures(tracks)
        val allGraphNodes = mutableListOf<BundleTrackFeatureItem>()
        for (recommendedTrack in tracks) {
            relationDataNodes.add(D3ForceNode(
                recommendedTrack.track.trackName,
                2,
                recommendedTrack.track.popularity / Config.recommendGraphPopularityFactor + 10,
                Config.nodeColorTrack
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
                relationDataLinks.add(D3ForceLinkDistance(
                    recommendedTrack.track.trackName,
                    recommendedTrack.features.getName(index),
                    0,
                    (100 - (normalize(
                        recommendedTrack.features.at(index)!!,
                        tracks.minOf { it.features.at(index)!! },
                        tracks.maxOf { it.features.at(index)!! }
                    )*100).roundToInt()) * Config.forceDistanceFactor
                ))
            }
        }
        for (index in 0 until tracks[0].features.count()){
            relationDataNodes.add(D3ForceNode(
                tracks[0].features.getName(index),
                1,
                Config.featureNodeRadius,
                Config.nodeColorFeature
            ))
            allGraphNodes.add(BundleTrackFeatureItem(null, listOf(), BundleItemType.FEATURE, AudioFeatureType.values()[index]))
        }
        return Triple(relationDataNodes, relationDataLinks, allGraphNodes)
    }

    suspend fun prepareGraphTopArtists(settings: GraphSettings?, topArtists: MutableList<Artist>, context: Context): ForceGraph {
        val edgesList = mutableListOf<Pair<Artist, Artist>>()
        val links = arrayListOf<D3ForceLink>()
        var edgesListGenres = mutableListOf<Pair<Artist,Artist>>()
        if (settings?.relatedFlag == true) {
            val edgesListRelated = getTopArtistsEdges(topArtists)
            edgesList.addAll(edgesListRelated)
            links.addAll(getLinks(edgesListRelated))
        }
        if (settings?.genresFlag == true) {
            edgesListGenres = getTopArtistsGenresEdges(topArtists)
            edgesList.addAll(edgesListGenres)
        }
        val nodeArtists =
            ((edgesList.map { it.first } + edgesList.map { it.second }) + topArtists).distinct()
        val genreColorMap = ColorHelper.gatherColorsForGenres(nodeArtists, context)
        val nodes = getNodesSizeBasedOnEdgesArtists(
            genreColorMap,
            nodeArtists,
            edgesList
        )
        if (settings?.genresFlag == true) {
            links.addAll(getLinksGenres(edgesListGenres, genreColorMap))
        }
        return ForceGraph(links, nodes, genreColorMap, nodeArtists, listOf(), BundleItemType.ARTIST)
    }

    suspend fun prepareGraphArtists(settings: GraphSettings, recommendedTracks: List<Track>, topArtists: List<Artist>, context: Context): ForceGraph {
        val edgesList = mutableListOf<Pair<Artist, Artist>>()
        val links = arrayListOf<D3ForceLink>()
        var edgesListGenres = mutableListOf<Pair<Artist,Artist>>()
        if (settings.relatedFlag) {
            val edgesListRelated = getEdges(recommendedTracks, topArtists)
            edgesList.addAll(edgesListRelated)
            links.addAll(getLinks(edgesListRelated))
        }
        if (settings.genresFlag) {
            edgesListGenres = getGenreEdges(recommendedTracks, topArtists)
            edgesList.addAll(edgesListGenres)
        }
        val nodeArtists =
            ((edgesList.map { it.first } + edgesList.map { it.second }) + recommendedTracks.map { it.artists[0] }).distinct()
        val genreColorMap = ColorHelper.gatherColorsForGenres(nodeArtists, context)
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

    suspend fun prepareGraphTracks(settings: GraphSettings?, recommendedTracksFeatures: List<TrackAudioFeatures>?, topTracks: List<TrackAudioFeatures>?, context: Context): ForceGraph {
        val edgesList = mutableListOf<Pair<Track, Track>>()
        val links = arrayListOf<D3ForceLink>()
        var edgesListGenres = mutableListOf<Pair<Track,Track>>()
        if (settings?.relatedFlag == true) {
            val edgesListRelated = getEdgesTracks(recommendedTracksFeatures?.map { it.track } ?: mutableListOf(), topTracks?.toMutableList() ?: mutableListOf())
            edgesList.addAll(edgesListRelated)
            links.addAll(getLinksTracks(edgesListRelated))
        }
        if (settings?.genresFlag == true) {
            edgesListGenres = getGenreEdgesTracks(recommendedTracksFeatures?.map { it.track } ?: mutableListOf(), topTracks?.toMutableList() ?: mutableListOf())
            edgesList.addAll(edgesListGenres)

        }
        if (settings?.featuresFlag == true) {
            val edgesListFeatures = getFeaturesEdges(recommendedTracksFeatures, topTracks?.toMutableList())
            edgesList.addAll(edgesListFeatures.map { it.tracks })
            links.addAll(getLinksFeatures(edgesListFeatures.map { it.tracks }.toMutableList()))
        }
        val nodeTracks =
            ((edgesList.map { it.first } + edgesList.map { it.second}) + (recommendedTracksFeatures?.map { it.track } ?: mutableListOf())).distinct()
        val genreColorMap = ColorHelper.gatherColorsForGenres(nodeTracks.map { it.artists[0] }, context)
        val nodes = prepareNodesTracks(
            genreColorMap,
            nodeTracks,
            topTracks?.map { it.track }?.toMutableList() ?: mutableListOf(),
            recommendedTracksFeatures?.map { it.track } ?: mutableListOf(),
            edgesList
        )
        if (settings?.genresFlag == true) {
            links.addAll(getLinksGenresTracks(edgesListGenres, genreColorMap))
        }
        return ForceGraph(links, nodes, genreColorMap, listOf(), nodeTracks, BundleItemType.TRACK)
    }
}