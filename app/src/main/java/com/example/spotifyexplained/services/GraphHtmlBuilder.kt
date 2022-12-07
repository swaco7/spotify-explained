package com.example.spotifyexplained.services

import com.example.spotifyexplained.general.Config
import com.example.spotifyexplained.model.*
import com.example.spotifyexplained.model.enums.ZoomType
import java.util.*

object GraphHtmlBuilder {
    fun buildFeaturesGraph(
        links: ArrayList<D3ForceLinkDistance>,
        nodes: ArrayList<D3ForceNode>,
        zoomType: ZoomType
    ): String {
        val rawHtml = NetworkFeaturesGraph.getHeader() +
                NetworkFeaturesGraph.addData(links.toString(), nodes.toString()) +
                NetworkFeaturesGraph.getMainSVG() +
                NetworkFeaturesGraph.getFeaturesSimulation(
                    Config.customRecommendOverallManyBody,
                    0.9f
                ) +
                NetworkFeaturesGraph.getFeaturesBody() +
                if (zoomType == ZoomType.RESPONSIVE) {
                    NetworkFeaturesGraph.getTickWithZoom() + NetworkFeaturesGraph.getZoomFeatures()
                } else {
                    NetworkFeaturesGraph.getTick() + NetworkFeaturesGraph.getZoom()
                } +
                NetworkFeaturesGraph.getHighlights() +
                NetworkGraph.getTextWrap() +
                NetworkFeaturesGraph.getFooter()
        return Base64.getEncoder().encodeToString(rawHtml.toByteArray())
    }

    fun buildBaseGraph(
        links: ArrayList<D3ForceLink>,
        nodes: ArrayList<D3ForceNode>,
        zoomType: ZoomType,
        strength: Int,
        collisionFactor: Float
    ) : String {
        val rawHtml = NetworkGraph.getHTMLCSSHeader() +
                NetworkGraph.addData(links.toString(), nodes.toString()) +
                NetworkGraph.getMainSVG() +
                NetworkGraph.getBaseSimulation(
                    strength,
                    collisionFactor
                ) +
                NetworkGraph.getBody() +
                NetworkGraph.getBundleLines() +
                if (zoomType == ZoomType.RESPONSIVE) {
                    NetworkGraph.getTickWithZoom() + NetworkGraph.getZoomFeatures()
                } else {
                    NetworkGraph.getTick() + NetworkGraph.getZoom()
                } +
                NetworkGraph.getHighlights() +
                NetworkGraph.getTextWrap() +
                NetworkGraph.getFooter()
        return Base64.getEncoder().encodeToString(rawHtml.toByteArray())
    }

    fun buildMetricsGraph(
        links: ArrayList<D3ForceLink>,
        nodes: ArrayList<D3ForceNode>,
        strength: Int,
        collisionFactor: Float
    ) : String {
        val rawHtml = NetworkGraph.getHTMLCSSHeader() +
                NetworkGraph.addData(links.toString(), nodes.toString()) +
                NetworkGraph.getMainSVG() +
//                NetworkGraph.getBaseSimulation(
//                    strength,
//                    collisionFactor
//                ) +
                MetricsBuilder.getMetrics(collisionFactor, strength) +
                MetricsBuilder.getReadability() +
                MetricsBuilder.getCoverage() +
                MetricsBuilder.getEdgeVariation() +
                NetworkGraph.getBody() +
                NetworkGraph.getTick() +
//                NetworkGraph.getZoom() +
//                NetworkGraph.getHighlights() +
                NetworkGraph.getTextWrap() +
                NetworkGraph.getFooter()
        return Base64.getEncoder().encodeToString(rawHtml.toByteArray())
    }
}