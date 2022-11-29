package com.example.spotifyexplained.general

interface JsWebInterfaceHandler {
    fun showDetailInfoFunc(message: String?)
    fun hideDetailInfoFunc()
    fun showBundleDetailInfo(tracks: String , message:String)
    fun finishLoading()
}