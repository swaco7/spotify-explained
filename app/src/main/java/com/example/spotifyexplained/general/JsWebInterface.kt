package com.example.spotifyexplained.general

import android.content.Context
import android.webkit.JavascriptInterface

class JsWebInterface(
    val context: Context,
    val showDetailInfo: (String) -> Unit,
    val hideDetailInfo: () -> Unit,
    val showTracksBundleDetailInfo: (String, String) -> Unit,
    val finishLoadingFunc: () -> Unit,
    val showLineDetailInfo: (String) -> Unit,
    val showMetricsInfo: ((String) -> Unit)? = null
) {
    @JavascriptInterface
    fun showGenreDetailInfo(message: String?) {
        showDetailInfo(message!!)
    }
    @JavascriptInterface
    fun hideGenreDetailInfo() {
        hideDetailInfo()
    }
    @JavascriptInterface
    fun showBundleDetailInfo(tracks: String , message:String) {
        showTracksBundleDetailInfo(tracks, message)
    }
    @JavascriptInterface
    fun finishLoading() {
        finishLoadingFunc()
    }
    @JavascriptInterface
    fun showLineDetail(message: String?) {
        showLineDetailInfo(message!!)
    }
    @JavascriptInterface
    fun showMetrics(message: String?) {
        showMetricsInfo?.invoke(message!!)
    }
}