package com.example.spotifyexplained.general

import com.example.spotifyexplained.model.*
import com.example.spotifyexplained.model.enums.AudioFeatureType
import com.example.spotifyexplained.model.enums.BundleItemType
import com.example.spotifyexplained.model.enums.LinkType

object GraphInfoHelper {
    fun showBundleDetailInfo(
        nodeIndices: String,
        currentIndex: String,
        nodesArtists: List<Artist>,
        nodesTracks: List<Track>,
        recommendedTracks: List<Track>,
        genreColorMap: HashMap<String, MixableColor>
    ): MutableList<BundleGraphItem> {
        val bundleItems = mutableListOf<BundleGraphItem>()
        if (nodesArtists.isNotEmpty()) {
            val artistIds = nodeIndices.split(',').map { artist -> nodesArtists[artist.toInt()] }
                .toMutableList()
            artistIds.add(nodesArtists[currentIndex.toInt()])
            for (artist in artistIds) {
                val recTrack =
                    recommendedTracks.firstOrNull { it.artists[0].artistId == artist.artistId }
                val genresList = Helper.getGenreColorList(artist, genreColorMap)
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
                val genresList = Helper.getGenreColorList(track.artists[0], genreColorMap)
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

    fun showLineDetailInfo(nodeIndices: String?, nodes: List<Artist>, nodesTracks: List<TrackAudioFeatures>): LineDetailInfo {
        val messageItems = nodeIndices!!.split(",")
        val sourceArtist: Artist
        val targetArtist: Artist
        if (nodes.isNotEmpty()) {
            sourceArtist = nodes[messageItems[0].toInt()]
            targetArtist = nodes[messageItems[1].toInt()]
        } else {
            sourceArtist = nodesTracks[messageItems[0].toInt()].track.artists[0]
            targetArtist = nodesTracks[messageItems[1].toInt()].track.artists[0]
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

    fun showLineDetailGenreInfo(nodeIndices: String?, nodes: List<Artist>, nodesTracks: List<TrackAudioFeatures>): LineDetailGenreInfo {
        val messageItems = nodeIndices!!.split(",")
        val sourceArtist: Artist
        val targetArtist: Artist
        if (nodes.isNotEmpty()) {
            sourceArtist = nodes[messageItems[0].toInt()]
            targetArtist = nodes[messageItems[1].toInt()]
        } else {
            sourceArtist = nodesTracks[messageItems[0].toInt()].track.artists[0]
            targetArtist = nodesTracks[messageItems[1].toInt()].track.artists[0]
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
            val similarity = Helper.featuresSimilar(
                sourceTrack.features.at(index)!!,
                targetTrack.features.at(index)!!
            )
            if (similarity > Config.featuresSimilarityThreshold)  {
                featuresList.add(Pair(AudioFeatureType.values()[index], similarity))
            }
        }
        return LineDetailFeatureInfo(
            sourceTrack,
            targetTrack,
            featuresList
        )
    }

    fun showBundleLineInfoTracks(
        nodeIndices: String?,
        nodesTracks: List<TrackAudioFeatures>,
        genreColorMap: HashMap<String, MixableColor>
    ): LineDetailBundleInfo {
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
                    items.add(
                        BundleLineInfo(
                            sourceArtist = sourceArtist,
                            targetArtist = targetArtist,
                            sourceTrack = sourceTrack,
                            targetTrack = targetTrack,
                            sToT = if (sourceToTarget == -1) null else sourceToTarget,
                            tToS = if (targetToSource == -1) null else targetToSource,
                            linkType = LinkType.RELATED
                        )
                    )
                }
                "GENRE" -> {
                    val intersection = sourceArtist.genres!!.intersect(targetArtist.genres!!.toMutableList().toSet()).toList()
                    for (genre in intersection) {
                        items.add(
                            BundleLineInfo(
                                sourceArtist = sourceArtist,
                                targetArtist = targetArtist,
                                sourceTrack = sourceTrack,
                                targetTrack = targetTrack,
                                genres = genre,
                                genreColor = Helper.getGenreColorList(genre, genreColorMap),
                                linkType = LinkType.GENRE
                            )
                        )
                    }
                }
                "FEATURE" -> {
                    val featuresList = mutableListOf<Pair<AudioFeatureType, Double>>()
                    for (index in 0 until AudioFeatureType.values().size){
                        val similarity = Helper.featuresSimilar(
                            sourceTrack.features.at(index)!!,
                            targetTrack.features.at(index)!!
                        )
                        if (similarity > Config.featuresSimilarityThreshold)  {
                            featuresList.add(Pair(AudioFeatureType.values()[index], similarity))
                        }
                    }
                    for (feature in featuresList) {
                        items.add(
                            BundleLineInfo(
                                sourceArtist = sourceArtist,
                                targetArtist = targetArtist,
                                sourceTrack = sourceTrack,
                                targetTrack = targetTrack,
                                features = feature,
                                linkType = LinkType.FEATURE
                            )
                        )
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
                    val sourceToTarget = sourceArtist.related_artists?.indexOf(targetArtist) ?: -1
                    val targetToSource = targetArtist.related_artists?.indexOf(sourceArtist) ?: -1
                    items.add(
                        BundleLineInfo(
                            sourceArtist = sourceArtist,
                            targetArtist = targetArtist,
                            sToT = if (sourceToTarget == -1) null else sourceToTarget,
                            tToS = if (targetToSource == -1) null else targetToSource,
                            linkType = LinkType.RELATED
                        )
                    )
                }
                "GENRE" -> {
                    val intersection = sourceArtist.genres!!.intersect(targetArtist.genres!!.toMutableList().toSet()).toList()
                    for (genre in intersection) {
                        items.add(
                            BundleLineInfo(
                                sourceArtist = sourceArtist,
                                targetArtist = targetArtist,
                                genres = genre,
                                genreColor = Helper.getGenreColorList(genre, genreColorMap),
                                linkType = LinkType.GENRE
                            )
                        )
                    }
                }
            }
        }
        return LineDetailBundleInfo(
            items
        )
    }
}