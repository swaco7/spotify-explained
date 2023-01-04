package com.example.spotifyexplained.html

import android.app.Activity
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.content.ContextCompat.getSystemService

object HorizontalBarChart {
    fun getBody(width: Int, height: Int) : String{
        return  "var margin = {top: 20, right: 20, bottom: 80, left: 30},\n" +
                "    width = $width - margin.left - margin.right,\n" +
                "    height = $height - margin.top - margin.bottom;\n" +
                "\n" +
                "// append the svg object to the body of the page\n" +
                "var svg = d3.select(\"#my_dataviz\")\n" +
                "  .append(\"svg\")\n" +
                "    .attr(\"width\", width + margin.left + margin.right)\n" +
                "    .attr(\"height\", height + margin.top + margin.bottom)\n" +
                "  .append(\"g\")\n" +
                "    .attr(\"transform\",\n" +
                "          \"translate(\" + margin.left + \",\" + margin.top + \")\");\n" +
                "\n" +
                "var color = d3.scaleOrdinal(d3.schemeCategory20);\n" +
                "  // Add X axis\n" +
                "  var x = d3.scaleBand()\n" +
                "    .range([ 0, width ])\n" +
                "    .domain(data.map(function(d) { return d.name; }))\n" +
                "    .padding(.2);\n" +

                "  svg.append(\"g\")\n" +
                "    .attr(\"transform\", \"translate(0,\" + height + \")\")\n" +
                "    .call(d3.axisBottom(x))\n" +
                "    .selectAll(\"text\")\n" +
                "      .attr(\"transform\", \"translate(-10,0)rotate(-45)\")\n" +
                "      .style(\"text-anchor\", \"end\")\n" +
                "       .text(function(d) { \n" +
                "           var doc = new DOMParser().parseFromString(d, \"text/html\");\n" +
                "           return doc.documentElement.textContent \n" +
                "    });\n" +
                "\n" +
                "  // Y axis\n" +
                "  var y = d3.scaleLinear()\n" +
                "    .domain([100, 0])\n" +
                "    .range([ 0, height]);\n" +
                "  svg.append(\"g\")\n" +
                "    .call(d3.axisLeft(y))\n" +
                "\n" +
                "  //Bars\n" +
                "  var bars = svg.selectAll(\"myRect\")\n" +
                "    .data(data)\n" +
                "    .enter()\n" +
                "    .append(\"g\")\n" +
                "  bars.append(\"rect\")\n" +
                "    .attr(\"y\", function(d) { return y(d.value);})\n" +
                "    .attr(\"x\", function(d) { return x(d.name); })\n" +
                "    .attr(\"height\", function(d) { return height - y(d.value); })\n" +
                "    .attr(\"width\", x.bandwidth() )\n" +
                "    .attr(\"fill\", function(d) { return color(d.group); });\n" +
                " \n" +
                "  bars.append(\"text\")\n" +
                "     .attr(\"class\", \"label\")\n" +
                "     .attr(\"y\", function(d) { return y(d.value) - 4; })\n" +
                "     .attr(\"x\", function(d) { return x(d.name) + x.bandwidth()/ 2 - 6; })\n" +
                "     .text(function (d) { return d.value; });\n" +
                "\n"

    }

    fun getHeader() : String {
        return  "<!DOCTYPE html>\n" +
                "<meta charset=\"utf-8\">\n" +
                "<script src=\"https://d3js.org/d3.v4.js\"></script>\n" +
                "<style>\n" +
                "  .label {\n" +
                "       font-size: 10px;\n" +
                "}\n" +
                "</style>" +
                "<div id=\"my_dataviz\"></div>\n" +
                "<script>"
    }

    fun getFooter(): String {
        return "</script>"
    }

}