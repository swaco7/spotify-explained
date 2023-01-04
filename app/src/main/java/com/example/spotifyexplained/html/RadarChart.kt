package com.example.spotifyexplained.html

object RadarChart {

    fun getBody() : String{
        return  "<script>\n" +
                "function RadarChart(id, data, options) {\n" +
                "  var cfg = {\n" +
                "    w: 300, //Width of the circle\n" +
                "    h: 300, //Height of the circle\n" +
                "    margin: {\n" +
                "      top: 40,\n" +
                "      right: 20,\n" +
                "      bottom: 20,\n" +
                "      left: 20\n" +
                "    }, //The margins of the SVG\n" +
                "    levels: 3, //How many levels or inner circles should there be drawn\n" +
                "    maxValue: 0, //What is the value that the biggest circle will represent\n" +
                "    labelFactor: 1.15, //How much farther than the radius of the outer circle should the labels be placed\n" +
                "    wrapWidth: 60, //The number of pixels after which a label needs to be given a new line\n" +
                "    dotRadius: 3, //The size of the colored circles of each blog\n" +
                "    opacityCircles: 0.1, //The opacity of the circles of each blob\n" +
                "    strokeWidth: 2, //The width of the stroke around each blob\n" +
                "    roundStrokes: false, //If true the area and stroke will follow a round path (cardinal-closed)\n" +
                "    color: d3.scale.category10() //Color function\n" +
                "  };\n" +
                "\n" +
                "  //Put all of the options into a variable called cfg\n" +
                "  if ('undefined' !== typeof options) {\n" +
                "    for (var i in options) {\n" +
                "      if ('undefined' !== typeof options[i]) {\n" +
                "        cfg[i] = options[i];\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "\n" +
                "  //If the supplied maxValue is smaller than the actual one, replace by the max in the data\n" +
                "  var maxValue = Math.max(cfg.maxValue, d3.max(data, function(i) {\n" +
                "    return d3.max(i.map(function(o) {\n" +
                "      return o.value;\n" +
                "    }))\n" +
                "  }));\n" +
                "\n" +
                "  var allAxis = (data[0].map(function(i, j) {\n" +
                "      return i.axis\n" +
                "    })), //Names of each axis\n" +
                "    total = allAxis.length, //The number of different axes\n" +
                "    radius = Math.min(cfg.w / 2, cfg.h / 2), //Radius of the outermost circle\n" +
                "    Format = d3.format('%'), //Percentage formatting\n" +
                "    angleSlice = Math.PI * 2 / total; //The width in radians of each \"slice\"\n" +
                "\n" +
                "  //Scale for the radius\n" +
                "  var rScale = d3.scale.linear()\n" +
                "    .range([0, radius])\n" +
                "    .domain([0, maxValue]);\n" +
                "\n" +
                "  //////////// Create the container SVG and g /////////////\n" +
                "  //Remove whatever chart with the same id/class was present before\n" +
                "  d3.select(id).select(\"svg\").remove();\n" +
                "\n" +
                "  //Initiate the radar chart SVG\n" +
                "  var svg = d3.select(id).append(\"svg\")\n" +
                "    .attr(\"width\", cfg.w + cfg.margin.left + cfg.margin.right)\n" +
                "    .attr(\"height\", cfg.h + cfg.margin.top + cfg.margin.bottom)\n" +
                "    .attr(\"class\", \"radar\" + id);\n" +
                "  //Append a g element\n" +
                "  var g = svg.append(\"g\")\n" +
                "    .attr(\"transform\", \"translate(\" + (cfg.w / 2 + cfg.margin.left) + \",\" + (cfg.h / 2 + cfg.margin.top) + \")\");\n" +
                "\n" +
//                "  var text = g.append(\"text\")\n" +
//                "    .attr(\"x\", 0)\n" +
//                "    .attr(\"y\", 0 - (height / 2) - 10)\n" +
//                "    .attr(\"class\", \"mtitle\")\n" +
//                "    .attr(\"text-anchor\", \"middle\")\n" +
//                "    .style(\"font-size\", \"16px\")\n" +
//                "    .text(tracks[selectedTitle]);\n" +
//                "    \n" +
                "  ////////// Glow filter for some extra pizzazz ///////////\n" +
                "\n" +
                "  //Filter for the outside glow\n" +
                "  var filter = g.append('defs').append('filter').attr('id', 'glow'),\n" +
                "    feGaussianBlur = filter.append('feGaussianBlur').attr('stdDeviation', '2.5').attr('result', 'coloredBlur'),\n" +
                "    feMerge = filter.append('feMerge'),\n" +
                "    feMergeNode_1 = feMerge.append('feMergeNode').attr('in', 'coloredBlur'),\n" +
                "    feMergeNode_2 = feMerge.append('feMergeNode').attr('in', 'SourceGraphic');\n" +
                "\n" +
                "  /////////////// Draw the Circular grid //////////////////\n" +
                "\n" +
                "  //Wrapper for the grid & axes\n" +
                "  var axisGrid = g.append(\"g\").attr(\"class\", \"axisWrapper\");\n" +
                "\n" +
                "  //Draw the background circles\n" +
                "  axisGrid.selectAll(\".levels\")\n" +
                "    .data(d3.range(1, (cfg.levels + 1)).reverse())\n" +
                "    .enter()\n" +
                "    .append(\"circle\")\n" +
                "    .attr(\"class\", \"gridCircle\")\n" +
                "    .attr(\"r\", function(d, i) {\n" +
                "      return radius / cfg.levels * d;\n" +
                "    })\n" +
                "    .style(\"fill\", \"#CDCDCD\")\n" +
                "    .style(\"stroke\", \"#CDCDCD\")\n" +
                "    .style(\"fill-opacity\", cfg.opacityCircles)\n" +
                "    .style(\"filter\", \"url(#glow)\");\n" +
                "\n" +
                "  //////////////////// Draw the axes //////////////////////\n" +
                "  //Create the straight lines radiating outward from the center\n" +
                "  var axis = axisGrid.selectAll(\".axis\")\n" +
                "    .data(allAxis)\n" +
                "    .enter()\n" +
                "    .append(\"g\")\n" +
                "    .attr(\"class\", \"axis\");\n" +
                "  //Append the lines\n" +
                "  axis.append(\"line\")\n" +
                "    .attr(\"x1\", 0)\n" +
                "    .attr(\"y1\", 0)\n" +
                "    .attr(\"x2\", function(d, i) {\n" +
                "      return rScale(maxValue * 1.1) * Math.cos(angleSlice * i - Math.PI / 2);\n" +
                "    })\n" +
                "    .attr(\"y2\", function(d, i) {\n" +
                "      return rScale(maxValue * 1.1) * Math.sin(angleSlice * i - Math.PI / 2);\n" +
                "    })\n" +
                "    .attr(\"class\", \"line\")\n" +
                "    .style(\"stroke\", \"white\")\n" +
                "    .style(\"stroke-width\", \"2px\");\n" +
                "\n" +
                "  //Append the labels at each axis\n" +
                "  axis.append(\"text\")\n" +
                "    .attr(\"class\", \"legend\")\n" +
                "    .style(\"font-size\", \"8px\")\n" +
                "    .attr(\"text-anchor\", \"middle\")\n" +
                "    .attr(\"dy\", \"0.35em\")\n" +
                "    .attr(\"x\", function(d, i) {\n" +
                "      return rScale(maxValue * cfg.labelFactor) * Math.cos(angleSlice * i - Math.PI / 2);\n" +
                "    })\n" +
                "    .attr(\"y\", function(d, i) {\n" +
                "      return rScale(maxValue * cfg.labelFactor) * Math.sin(angleSlice * i - Math.PI / 2);\n" +
                "    })\n" +
                "    .text(function(d) {\n" +
                "      return d\n" +
                "    })\n" +
                "    .call(wrap, cfg.wrapWidth);\n" +
                "\n" +
                "  ///////////// Draw the radar chart blobs ////////////////\n" +
                "  //The radial line function\n" +
                "  var radarLine = d3.svg.line.radial()\n" +
                "    .interpolate(\"linear-closed\")\n" +
                "    .radius(function(d) {\n" +
                "      return rScale(d.value);\n" +
                "    })\n" +
                "    .angle(function(d, i) {\n" +
                "      return i * angleSlice;\n" +
                "    });\n" +
                "\n" +
                "  if (cfg.roundStrokes) {\n" +
                "    radarLine.interpolate(\"cardinal-closed\");\n" +
                "  }\n" +
                "\n" +
                "  //Create a wrapper for the blobs\n" +
                "  var blobWrapper = g.selectAll(\".radarWrapper\")\n" +
                "    .data(data)\n" +
                "    .enter().append(\"g\")\n" +
                "    .attr(\"class\", \"radarWrapper\");\n" +
                "\n" +
                "  //Append the backgrounds\n" +
                "  blobWrapper\n" +
                "    .append(\"path\")\n" +
                "    .attr(\"class\", \"radarArea\")\n" +
                "    .attr(\"d\", function(d, i) {\n" +
                "      return radarLine(d);\n" +
                "    })\n" +
                "    .style(\"fill\", function(d, i) {\n" +
                "      return cfg.color(i);\n" +
                "    })\n" +
                "    .style(\"fill-opacity\", function(d, i) {\n" +
                "      return (i == selected) ? 0.85 : 0.10;\n" +
                "    })\n" +
                "\n" +
                "  //Create the outlines\n" +
                "  blobWrapper.append(\"path\")\n" +
                "    .attr(\"class\", \"radarStroke\")\n" +
                "    .attr(\"d\", function(d, i) {\n" +
                "      return radarLine(d);\n" +
                "    })\n" +
                "    .style(\"stroke-width\", cfg.strokeWidth + \"px\")\n" +
                "    .style(\"stroke\", function(d, i) {\n" +
                "      return cfg.color(i);\n" +
                "    })\n" +
                "    .style(\"stroke-opacity\", function(d, i) {\n" +
                "      return (i == selected) ? 0.85 : 0.10;\n" +
                "    })\n" +
                "    .style(\"fill\", \"none\")\n" +
                "    .style(\"filter\", \"url(#glow)\");\n" +
                "\n" +
                "  //Append the circles\n" +
                "  blobWrapper.selectAll(\".radarCircle\")\n" +
                "    .data(function(d, i) {\n" +
                "      return d;\n" +
                "    })\n" +
                "    .enter().append(\"circle\")\n" +
                "    .attr(\"class\", \"radarCircle\")\n" +
                "    .attr(\"r\", cfg.dotRadius)\n" +
                "    .attr(\"cx\", function(d, i) {\n" +
                "      return rScale(d.value) * Math.cos(angleSlice * i - Math.PI / 2);\n" +
                "    })\n" +
                "    .attr(\"cy\", function(d, i) {\n" +
                "      return rScale(d.value) * Math.sin(angleSlice * i - Math.PI / 2);\n" +
                "    })\n" +
                "    .style(\"fill\", function(d, i, j) {\n" +
                "      return cfg.color(j);\n" +
                "    })\n" +
                "    .style(\"fill-opacity\", function(d, i) {\n" +
                "      return (i == selected) ? 0.85 : 0.5;\n" +
                "    })\n" +
                "\n" +
                "  //////// Append invisible circles for tooltip ///////////\n" +
                "  //Wrapper for the invisible circles on top\n" +
                "  var blobCircleWrapper = g.selectAll(\".radarCircleWrapper\")\n" +
                "    .data(data)\n" +
                "    .enter().append(\"g\")\n" +
                "    .attr(\"class\", \"radarCircleWrapper\");\n" +
                "\n" +
                "  var blobTexts = blobCircleWrapper.selectAll(\".radarCircleWrapper\")\n" +
                "    .data(function(d, i) {\n" +
                "      return d;\n" +
                "    })\n" +
                "    .enter().append(\"text\")\n" +
                "    .attr(\"class\", \"tooltip\")\n" +
                "    .attr('x', function(d, i) {\n" +
                "      var base = rScale(d.value) * Math.cos(angleSlice * i - Math.PI / 2)\n" +
                "      return (i < 5) ? base : base - 15\n" +
                "    })\n" +
                "    .attr('y', function(d, i) {\n" +
                "      var base = rScale(d.value) * Math.sin(angleSlice * i - Math.PI / 2)\n" +
                "      return (i < 5) ? base - 5 : base - 5\n" +
                "    })\n" +
                "    .text(function(d, i) {\n" +
                "      return Format(d.value)\n" +
                "    })\n" +
                "    .transition().duration(200)\n" +
                "    .style('opacity', 1);\n" +
                "\n" +
                "  blobCircleWrapper.style(\"opacity\", function(d, i) {\n" +
                "    return i == selected ? 1 : 0\n" +
                "  })\n" +
                "\n" +
                "  /////////////////// Helper Function /////////////////////\n" +
                "  //Taken from http://bl.ocks.org/mbostock/7555321\n" +
                "  //Wraps SVG text\n" +
                "  function wrap(text, width) {\n" +
                "    text.each(function() {\n" +
                "      var text = d3.select(this),\n" +
                "        words = text.text().split(/\\s+/).reverse(),\n" +
                "        word,\n" +
                "        line = [],\n" +
                "        lineNumber = 0,\n" +
                "        lineHeight = 1.4, // ems\n" +
                "        y = text.attr(\"y\"),\n" +
                "        x = text.attr(\"x\"),\n" +
                "        dy = parseFloat(text.attr(\"dy\")),\n" +
                "        tspan = text.text(null).append(\"tspan\").attr(\"x\", x).attr(\"y\", y).attr(\"dy\", dy + \"em\");\n" +
                "\n" +
                "      while (word = words.pop()) {\n" +
                "        line.push(word);\n" +
                "        tspan.text(line.join(\" \"));\n" +
                "        if (tspan.node().getComputedTextLength() > width) {\n" +
                "          line.pop();\n" +
                "          tspan.text(line.join(\" \"));\n" +
                "          line = [word];\n" +
                "          tspan = text.append(\"tspan\").attr(\"x\", x).attr(\"y\", y).attr(\"dy\", ++lineNumber * lineHeight + dy + \"em\").text(word);\n" +
                "        }\n" +
                "      }\n" +
                "    });\n" +
                "  }\n" +
                "\n" +
                "}"
    }

    fun getHeader() : String {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "<meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\"/ >\n" +
                "<title>Smoothed D3.js Radar Chart</title>\n" +
                "\n" +
                "<!-- Google fonts -->\n" +
                "<link href=\"https://fonts.googleapis.com/css?family=Open+Sans:400,300\" rel='stylesheet' type='text/css'>\n" +
                "<link href='https://fonts.googleapis.com/css?family=Raleway' rel='stylesheet' type='text/css'>\n" +
                "\n" +
                "<!-- D3.js -->\n" +
                "<script src=\"https://cdnjs.cloudflare.com/ajax/libs/d3/3.5.6/d3.min.js\" charset=\"utf-8\"></script>\n" +
                "\n" +
                "<style>\n" +
                "body {\n" +
                "font-family: 'Open Sans', sans-serif;\n" +
                "font-size: 11px;\n" +
                "font-weight: 300;\n" +
                "fill: #242424;\n" +
                "text-align: center;\n" +
                "text-shadow: 0 1px 0 #fff, 1px 0 0 #fff, -1px 0 0 #fff, 0 -1px 0 #fff;\n" +
                "cursor: default;\n" +
                "}\n" +
                "\n" +
                ".legend {\n" +
                "font-family: 'Raleway', sans-serif;\n" +
                "fill: #333333;\n" +
                "}\n" +
                "\n" +
                ".tooltip {\n" +
                "fill: #333333;\n" +
                "}\n" +
                "</style>\n" +
                "\n" +
                "</head>\n" +
                "<body>\n" +
                "\n" +
                "<div class=\"radarChart\"></div>\n"
    }

    fun getFooter(): String {
        return "</body>\n" +
                "</html>"
    }

    fun getChartDesignHeader(width: Int, selected: Int, selectedTitle: Int, color: List<String>): String {
        return  "/* Radar chart design created by Nadieh Bremer - VisualCinnamon.com */\n" +
                "//////////////////////// Set-Up ////////////////////////////// \n" +
                "var margin = {\n" +
                "    top: 30,\n" +
                "    right: 40,\n" +
                "    bottom: 0,\n" +
                "    left: 40\n" +
                "  },\n" +
                "  width = $width,\n" +
                "  height = $width + 50;\n" +
                "\n" +
                "//////////////////// Draw the Chart /////////////\n" +
                "var color = d3.scale.ordinal()\n" +
                "  .range([\"${color.joinToString("\",\"")}\"]);\n" +
                "\n" +
                "var selectedTitle = $selectedTitle\n" +
                "var selected = $selected\n" +
                "var radarChartOptions = {\n" +
                "  w: width,\n" +
                "  h: height,\n" +
                "  margin: margin,\n" +
                "  maxValue: 1,\n" +
                "  levels: 5,\n" +
                "  roundStrokes: true,\n" +
                "  color: color,\n" +
                "  selected: selected,\n" +
                "  selectedTitle: selectedTitle\n" +
                "};\n"
    }

    fun getChartDesignFooter(): String {
        return "RadarChart(\".radarChart\", data, radarChartOptions);\n" +
                "</script>"
    }
}