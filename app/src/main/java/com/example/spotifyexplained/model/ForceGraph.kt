package com.example.spotifyexplained.model

data class ForceGraph (
    val links : ArrayList<D3Link>,
    val nodes : ArrayList<D3Node>,
    val genreColorMap: HashMap<String, MixableColor>,
    val nodeArtists : List<Artist>,
    val nodeTracks : List<Track>,
    val nodeType : BundleItemType
)