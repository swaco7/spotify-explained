package com.example.spotifyexplained.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.spotifyexplained.R
import com.highsoft.highcharts.common.hichartsclasses.*
import com.highsoft.highcharts.core.HIChartView
import java.util.*
import kotlin.collections.ArrayList


class HighchartsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_highcharts)

        val chartView = findViewById<HIChartView>(R.id.hc)

        chartView.plugins = ArrayList()
        chartView.plugins.add("networkgraph")

        val options = HIOptions()

        val chart = HIChart()
        chart.type = "networkgraph"
        chart.height = "100%"
        options.chart = chart

        val title = HITitle()
        title.text = "The Indo-European Laungauge Tree"
        options.title = title

        val subtitle = HISubtitle()
        subtitle.text = "A Force-Directed Network Graph in Highcharts"
        options.subtitle = subtitle

        val plotoptions = HIPlotOptions()
        plotoptions.networkgraph = HINetworkgraph()
        plotoptions.networkgraph.keys = ArrayList(Arrays.asList("from", "to"))
        plotoptions.networkgraph.layoutAlgorithm = HILayoutAlgorithm()
        plotoptions.networkgraph.layoutAlgorithm.enableSimulation = true
        options.plotOptions = plotoptions

        val networkgraph = HINetworkgraph()
        //networkgraph.setDataLabels(HIDataLabels())
        //networkgraph.dataLabels = HIDataLabels()

        val data: ArrayList<Array<String>> = ArrayList(
            Arrays.asList(
                arrayOf("Proto Indo-European", "Balto-Slavic"),
                arrayOf("Proto Indo-European", "Germanic"),
                arrayOf("Proto Indo-European", "Celtic"),
                arrayOf("Proto Indo-European", "Italic"),
                arrayOf("Proto Indo-European", "Hellenic"),
                arrayOf("Proto Indo-European", "Anatolian"),
                arrayOf("Proto Indo-European", "Indo-Iranian"),
                arrayOf("Proto Indo-European", "Tocharian"),
                arrayOf("Indo-Iranian", "Dardic"),
                arrayOf("Indo-Iranian", "Indic"),
                arrayOf("Indo-Iranian", "Iranian"),
                arrayOf("Iranian", "Old Persian"),
                arrayOf("Old Persian", "Middle Persian"),
                arrayOf("Indic", "Sanskrit"),
                arrayOf("Italic", "Osco-Umbrian"),
                arrayOf("Italic", "Latino-Faliscan"),
                arrayOf("Latino-Faliscan", "Latin"),
                arrayOf("Celtic", "Brythonic"),
                arrayOf("Celtic", "Goidelic"),
                arrayOf("Germanic", "North Germanic"),
                arrayOf("Germanic", "West Germanic"),
                arrayOf("Germanic", "East Germanic"),
                arrayOf("North Germanic", "Old Norse"),
                arrayOf("North Germanic", "Old Swedish"),
                arrayOf("North Germanic", "Old Danish"),
                arrayOf("West Germanic", "Old English"),
                arrayOf("West Germanic", "Old Frisian"),
                arrayOf("West Germanic", "Old Dutch"),
                arrayOf("West Germanic", "Old Low German"),
                arrayOf("West Germanic", "Old High German"),
                arrayOf("Old Norse", "Old Icelandic"),
                arrayOf("Old Norse", "Old Norwegian"),
                arrayOf("Old Norwegian", "Middle Norwegian"),
                arrayOf("Old Swedish", "Middle Swedish"),
                arrayOf("Old Danish", "Middle Danish"),
                arrayOf("Old English", "Middle English"),
                arrayOf("Old Dutch", "Middle Dutch"),
                arrayOf("Old Low German", "Middle Low German"),
                arrayOf("Old High German", "Middle High German"),
                arrayOf("Balto-Slavic", "Baltic"),
                arrayOf("Balto-Slavic", "Slavic"),
                arrayOf("Slavic", "East Slavic"),
                arrayOf("Slavic", "West Slavic"),
                arrayOf("Slavic", "South Slavic"),
                arrayOf("Proto Indo-European", "Phrygian"),
                arrayOf("Proto Indo-European", "Armenian"),
                arrayOf("Proto Indo-European", "Albanian"),
                arrayOf("Proto Indo-European", "Thracian"),
                arrayOf("Tocharian", "Tocharian A"),
                arrayOf("Tocharian", "Tocharian B"),
                arrayOf("Anatolian", "Hittite"),
                arrayOf("Anatolian", "Palaic"),
                arrayOf("Anatolian", "Luwic"),
                arrayOf("Anatolian", "Lydian"),
                arrayOf("Iranian", "Balochi"),
                arrayOf("Iranian", "Kurdish"),
                arrayOf("Iranian", "Pashto"),
                arrayOf("Iranian", "Sogdian"),
                arrayOf("Old Persian", "Pahlavi"),
                arrayOf("Middle Persian", "Persian"),
                arrayOf("Hellenic", "Greek"),
                arrayOf("Dardic", "Dard"),
                arrayOf("Sanskrit", "Sindhi"),
                arrayOf("Sanskrit", "Romani"),
                arrayOf("Sanskrit", "Urdu"),
                arrayOf("Sanskrit", "Hindi"),
                arrayOf("Sanskrit", "Bihari"),
                arrayOf("Sanskrit", "Assamese"),
                arrayOf("Sanskrit", "Bengali"),
                arrayOf("Sanskrit", "Marathi"),
                arrayOf("Sanskrit", "Gujarati"),
                arrayOf("Sanskrit", "Punjabi"),
                arrayOf("Sanskrit", "Sinhalese"),
                arrayOf("Osco-Umbrian", "Umbrian"),
                arrayOf("Osco-Umbrian", "Oscan"),
                arrayOf("Latino-Faliscan", "Faliscan"),
                arrayOf("Latin", "Portugese"),
                arrayOf("Latin", "Spanish"),
                arrayOf("Latin", "French"),
                arrayOf("Latin", "Romanian"),
                arrayOf("Latin", "Italian"),
                arrayOf("Latin", "Catalan"),
                arrayOf("Latin", "Franco-Provençal"),
                arrayOf("Latin", "Rhaeto-Romance"),
                arrayOf("Brythonic", "Welsh"),
                arrayOf("Brythonic", "Breton"),
                arrayOf("Brythonic", "Cornish"),
                arrayOf("Brythonic", "Cuymbric"),
                arrayOf("Goidelic", "Modern Irish"),
                arrayOf("Goidelic", "Scottish Gaelic"),
                arrayOf("Goidelic", "Manx"),
                arrayOf("East Germanic", "Gothic"),
                arrayOf("Middle Low German", "Low German"),
                arrayOf("Middle High German", "(High) German"),
                arrayOf("Middle High German", "Yiddish"),
                arrayOf("Middle English", "English"),
                arrayOf("Middle Dutch", "Hollandic"),
                arrayOf("Middle Dutch", "Flemish"),
                arrayOf("Middle Dutch", "Dutch"),
                arrayOf("Middle Dutch", "Limburgish"),
                arrayOf("Middle Dutch", "Brabantian"),
                arrayOf("Middle Dutch", "Rhinelandic"),
                arrayOf("Old Frisian", "Frisian"),
                arrayOf("Middle Danish", "Danish"),
                arrayOf("Middle Swedish", "Swedish"),
                arrayOf("Middle Norwegian", "Norwegian"),
                arrayOf("Old Norse", "Faroese"),
                arrayOf("Old Icelandic", "Icelandic"),
                arrayOf("Baltic", "Old Prussian"),
                arrayOf("Baltic", "Lithuanian"),
                arrayOf("Baltic", "Latvian"),
                arrayOf("West Slavic", "Polish"),
                arrayOf("West Slavic", "Slovak"),
                arrayOf("West Slavic", "Czech"),
                arrayOf("West Slavic", "Wendish"),
                arrayOf("East Slavic", "Bulgarian"),
                arrayOf("East Slavic", "Old Church Slavonic"),
                arrayOf("East Slavic", "Macedonian"),
                arrayOf("East Slavic", "Serbo-Croatian"),
                arrayOf("East Slavic", "Slovene"),
                arrayOf("South Slavic", "Russian"),
                arrayOf("South Slavic", "Ukrainian"),
                arrayOf("South Slavic", "Belarusian"),
                arrayOf("South Slavic", "Rusyn")
            )
        )
        networkgraph.data = data

        options.series = ArrayList(Collections.singletonList(networkgraph))

        chartView.options = options
    }
}