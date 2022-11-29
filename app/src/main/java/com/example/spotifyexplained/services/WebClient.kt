package com.example.spotifyexplained.services

import android.os.Build
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import java.util.*


class WebClient : WebViewClient() {
    var source : String = ""

    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        view.loadUrl(url)
        return true
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onPageFinished(view: WebView, url: String) {
        // Obvious next step is: document.forms[0].submit()
        var htmlString = ""
        view.evaluateJavascript(
            "(function() { return (document.getElementsByTagName('video')[0].innerHTML); })();"
        ) { html ->
            source = html.substringAfter("src=").substringBefore("type").trim()
            Log.d("HTML", source)
        }
    }
}