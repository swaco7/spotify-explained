package com.example.spotifyexplained.html

object WordCloud {
    fun getHeader(): String {
        return "<!DOCTYPE html>\n" +
                "<meta charset=\"utf-8\">\n" +
                "\n" +
                "<!-- Load d3.js -->\n" +
                "<script src=\"https://d3js.org/d3.v4.js\"></script>\n" +
                "\n" +
                "<!-- Load d3-cloud -->\n" +
                "<script src=\"https://cdn.jsdelivr.net/gh/holtzy/D3-graph-gallery@master/LIB/d3.layout.cloud.js\"></script>\n" +
                "\n" +
                "<!-- Create a div where the graph will take place -->\n" +
                "<div id=\"my_dataviz\"></div>\n" +
                "<script>\n"
    }
    fun getBody(width: Int): String{
        return "// set the dimensions and margins of the graph\n" +
                "var margin = {top: 0, right: 5, bottom: 0, left: 0},\n" +
                "    width = $width - margin.left - margin.right,\n" +
                "    height = $width - margin.top - margin.bottom;\n" +
                "\n" +
                "// append the svg object to the body of the page\n" +
                "var svg = d3.select(\"#my_dataviz\").append(\"svg\")\n" +
                "    .attr(\"width\", width + margin.left + margin.right)\n" +
                "    .attr(\"height\", height + margin.top + margin.bottom)\n" +
                "  .append(\"g\")\n" +
                "    .attr(\"transform\",\n" +
                "          \"translate(\" + margin.left + \",\" + margin.top + \")\");\n" +
                "\n" +
                "// Constructs a new cloud layout instance. It run an algorithm to find the position of words that suits your requirements\n" +
                "var layout = d3.layout.cloud()\n" +
                "  .size([width, height])\n" +
                "  .words(data.map(function(d) { return {text: d.text, size: d.size}; }))\n" +
                "  .padding(2)\n" +
                "  .fontSize(function(d) { return d.size; }) \n" +
                "  .font(\"Impact\")\n" +
                "  .rotate(function() { return ((Math.random() * 2)-1) * 30; })\n" +
                "  .on(\"end\", draw);\n" +
                "layout.start();\n" +
                "\n" +
                "// This function takes the output of 'layout' above and draw the words\n" +
                "// Better not to touch it. To change parameters, play with the 'layout' variable above\n" +
                "function draw(words) {\n" +
                "  svg\n" +
                "    .append(\"g\")\n" +
                "      .attr(\"transform\", \"translate(\" + layout.size()[0] / 2 + \",\" + layout.size()[1] / 2 + \")\")\n" +
                "      .selectAll(\"text\")\n" +
                "        .data(words)\n" +
                "      .enter().append(\"text\")\n" +
                "        .style(\"font-size\", function(d) { return d.size; })\n" +
                "        .style(\"fill\", \"#69b3a2\")\n" +
                "        .attr(\"text-anchor\", \"middle\")\n" +
                "        .attr(\"font-weight\", 900)\n" +
                "        .attr(\"transform\", function(d) {\n" +
                "          return \"translate(\" + [d.x, d.y] + \")rotate(\" + d.rotate + \")\";\n" +
                "        })\n" +
                "        .text(function(d) { return d.text; });\n" +
                "}\n"
    }

    fun getFooter(): String {
        return "</script>"
    }
}