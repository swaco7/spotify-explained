package com.example.spotifyexplained.model

import com.example.spotifyexplained.model.enums.BundleItemType

data class ForceGraph (
    val links : ArrayList<D3ForceLink>,
    val nodes : ArrayList<D3ForceNode>,
    val genreColorMap: HashMap<String, MixableColor>,
    val nodeArtists : List<Artist>,
    val nodeTracks : List<Track>,
    val nodeType : BundleItemType
)