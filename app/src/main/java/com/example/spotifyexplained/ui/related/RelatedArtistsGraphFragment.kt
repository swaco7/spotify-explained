package com.example.spotifyexplained.ui.related

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import com.example.spotifyexplained.activity.MainActivity
import com.example.spotifyexplained.databinding.FragmentRelatedArtistsGraphBinding
import com.example.spotifyexplained.model.D3Link
import com.example.spotifyexplained.model.D3Node
import com.example.spotifyexplained.services.NetworkGraph
import com.example.spotifyexplained.ui.saved.ContextViewModelFactory
import java.util.*
import kotlin.collections.ArrayList

class RelatedArtistsGraphFragment() : Fragment() {
    private lateinit var viewModel: RelatedArtistsGraphViewModel
    private var _binding: FragmentRelatedArtistsGraphBinding? = null
    private lateinit var webView : WebView

    private val binding get() = _binding!!

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentRelatedArtistsGraphBinding.inflate(inflater, container, false)
        webView = binding.webView
        viewModel = ViewModelProvider(this, ContextViewModelFactory(context as MainActivity))[RelatedArtistsGraphViewModel::class.java]
        viewModel.artistsLinks.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                drawD3Graph(viewModel.artistsNodes.value!!, viewModel.artistsLinks.value!!)
            }
        }
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun drawD3Graph(nodes: ArrayList<D3Node>, links: ArrayList<D3Link>) {
        webView.settings.javaScriptEnabled = true
        val unencodedHtml = NetworkGraph.getHeader() +
                "var nodes = $nodes; \n" +
                "var links = $links; \n" +
                NetworkGraph.getMainSVG() +
                NetworkGraph.getBaseSimulation(-10, 1.5f) +
                NetworkGraph.getBody() +
                NetworkGraph.getZoom() +
                NetworkGraph.getDrag() +
                NetworkGraph.getFooter()
        val encodedHtml = Base64.getEncoder().encodeToString(unencodedHtml.toByteArray())
        webView.loadData(encodedHtml, "text/html", "base64")
    }
}