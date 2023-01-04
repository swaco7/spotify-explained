package com.example.spotifyexplained.html

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
                    Config.featuresGraphCollisions,
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

    fun buildFeaturesMetricsGraph(
        links: ArrayList<D3ForceLinkDistance>,
        nodes: ArrayList<D3ForceNode>,
        strength: Int,
        collisionFactor: Float,
        dataProvided: Boolean,
        data: String?,
    ) : String {
        val rawHtml = NetworkFeaturesGraph.getHeader() +
                if (!dataProvided) {
                    NetworkFeaturesGraph.addData(links.toString(), nodes.toString())
                } else {
                    data
                } +
                NetworkFeaturesGraph.getMainSVG() +
                MetricsBuilder.getFeatureMetrics(collisionFactor, strength) +
                MetricsBuilder.getCoverage() +
                MetricsBuilder.getOverlap() +
                MetricsBuilder.getDistanceDistortion() +
                NetworkFeaturesGraph.getFeaturesBody() +
                NetworkFeaturesGraph.getTick() + NetworkFeaturesGraph.getZoom() +
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
                    collisionFactor,
                    zoomType == ZoomType.RESPONSIVE
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
        collisionFactor: Float,
        dataProvided: Boolean,
        data: String?
    ) : String {
        val rawHtml = NetworkGraph.getHTMLCSSHeader() +
                if (!dataProvided) {
                    NetworkGraph.addData(links.toString(), nodes.toString())
                } else {
                    data
                } +
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