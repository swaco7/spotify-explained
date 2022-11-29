package com.example.spotifyexplained.services

object NetworkGraph {
    fun getMainSVG(): String {
        return "var svg = d3.select(\"svg\"),\n" +
                "   width = +svg.attr(\"width\"),\n" +
                "   height = +svg.attr(\"height\");\n" +
                "\n"
    }

    fun getBaseSimulation(strength: Int, collisionFactor: Float): String {
        return "var simulation = d3.forceSimulation()\n" +
                "    .force(\"link\", d3.forceLink().id(function(d) { return d.id; }))\n" +
                "    .force(\"charge\", d3.forceManyBody().strength($strength))\n" +
                "    .force(\"center\", d3.forceCenter(width / 2, height / 2))\n" +
                "    .force(\"collision\", d3.forceCollide().radius(function(d) {\n" +
                "       return d.radius * $collisionFactor;\n" +
                "     }))\n" +
                ".on(\"end\", updateNodeSizes);\n" +
                "\n" +
                "function updateNodeSizes() {\n" +
                "  mKeys = keys\n" +
                "  getCollisions()\n" +
                "  addArtificialLines()\n" +
                "  updateVisual()\n" +
                "  finishLoading()\n" +
                "}\n"
    }

    fun getFeaturesSimulation(strength: Int): String {
        return "var simulation = d3.forceSimulation()\n" +
                "    .force(\"link\", d3.forceLink().id(function(d) { return d.id; }).distance(function(d) { return d.distance; }))\n" +
                "    .force(\"charge\", d3.forceManyBody().strength($strength))\n" +
                "    .force(\"center\", d3.forceCenter(width / 2, height / 2))\n" +
                "    .force(\"collision\", d3.forceCollide().radius(function(d) {\n" +
                "       return d.radius * 0.8;\n" +
                "     }))\n" +
                ".on(\"end\", updateNodeSizes);\n" +
                "\n" +
                "function updateNodeSizes() {\n" +
                "  finishLoading()\n" +
                "}\n"
    }

    fun addData(links: String, nodes: String): String {
        return  "var nodes = $nodes; \n" +
                "var links = $links; \n"
    }
    fun getBody() : String {
        return " var color = d3.scaleLinear().domain([1,2])\n" +
                "    .range([\"black\", \"white\"])\n" +
                "\n" +
                " var gGenres = svg.append(\"g\")\n" +
                "  .attr(\"class\", \"wrapper\")\n" +
                "  .attr(\"transform\", d3.zoomIdentity.translate(100, 50).scale(0.5));\n" +
                " var gFirst = svg.append(\"g\")\n" +
                "  .attr(\"class\", \"wrapper\")\n" +
                "  .attr(\"transform\", d3.zoomIdentity.translate(100, 50).scale(0.5));\n" +
                " var g = svg.append(\"g\")\n" +
                "    .attr(\"class\", \"wrapper\")\n" +
                "    .attr(\"transform\", d3.zoomIdentity.translate(100, 50).scale(0.5));\n" +
                "\n" +
                // Genre Arcs //
                "  var linkArc = gGenres.append(\"g\")\n" +
                "  .attr(\"class\", \"links\")\n" +
                "  .selectAll(\"line\")\n" +
                "  .data(links)\n" +
                "  .enter().append(\"svg:path\")\n" +
                "  .filter(function(d) { return d.type == \"GENRE\" })\n" +
                "  .on(\"mouseover\", function(d) {\n" +
                "    linkArc.style(\"stroke\", function(o) {\n" +
                "      return (o === d) ? \"red\" : o.color\n" +
                "    })\n" +
                "    showLineDetail(nodes.indexOf(d.source) + \",\" + nodes.indexOf(d.target) + \",\" + d.value + \",\" + \"GENRE\")\n" +
                "  })\n" +
                "  .on(\"mouseout\", function(d) {\n" +
                "    linkArc.style(\"stroke\", function(o) {\n" +
                "      return (o === d) ? d.color : o.color\n" +
                "    })\n" +
                "    hideDetail(d.source.id);\n" +
                "  })\n" +
                "  .attr(\"stroke\", function(d){\n" +
                "  \treturn d.color;\n" +
                "  })\n" +
                "  .attr(\"stroke-opacity\", 0.6)\n" +
                "  .attr(\"fill\", \"transparent\")\n" +
                "  .attr(\"stroke-width\", function(d) {\n" +
                "    return Math.sqrt(d.value);\n" +
                "  });\n" +
                "\n" +
                // Relation Links //
                " var link = g.append(\"g\")\n" +
                "    .attr(\"class\", \"links\")\n" +
                "    .selectAll(\"line\")\n" +
                "    .data(links)\n" +
                "    .enter().append(\"line\")\n" +
                "    .filter(function(d) { return d.type == \"RELATED\" })\n" +
                "    .on(\"mouseover\", function(d) {\n" +
                "       link.style(\"stroke\", function(o){\n" +
                "           return (o === d) ? \"red\" : \"#999\"\n" +
                "       })\n" +
                "    showLineDetail(nodes.indexOf(d.source) + \",\" + nodes.indexOf(d.target) + \",\" + d.value + \",\" + \"RELATED\")\n" +
                "    })\n" +
                "    .on(\"mouseout\", function(d) {\n" +
                "       link.style(\"stroke\", \"#999\")\n" +
                "       hideDetail(d.source.id);\n" +
                "    })\n" +
                "    .attr(\"stroke-width\", function(d) { return Math.sqrt(d.value); });\n" +
                // Features Links //
                "   var linkFeatures = g.append(\"g\")\n" +
                "  .attr(\"class\", \"links\")\n" +
                "  .selectAll(\"line\")\n" +
                "  .data(links)\n" +
                "  .enter().append(\"line\")\n" +
                "   .filter(function(d) { return d.type == \"FEATURE\" })\n" +
                "  .on(\"mouseover\", function(d) {\n" +
                "    linkFeatures.style(\"stroke\", function(o) {\n" +
                "      return (o === d) ? \"red\" : \"#999\"\n" +
                "    })\n" +
                "    showLineDetail(nodes.indexOf(d.source) + \",\" + nodes.indexOf(d.target) + \",\" + d.value + \",\" + \"FEATURE\")\n" +
                "  })\n" +
                "  .on(\"mouseout\", function(d) {\n" +
                "    linkFeatures.style(\"stroke\", \"#999\")\n" +
                "    hideDetail(d.source.id);\n" +
                "  })\n" +
                "  .attr(\"stroke-dasharray\", function(o) {\n" +
                "      return (\"3, 1\")\n" +
                "    })\n" +
                "  .attr(\"stroke-width\", function(d) {\n" +
                "    return Math.sqrt(d.value);\n" +
                "  });\n" +
                "  \n" +
                // Nodes
                " var node = g.append(\"g\")\n" +
                "    .attr(\"class\", \"nodes\")\n" +
                "    .selectAll(\"g\")\n" +
                "    .data(nodes)\n" +
                "    .enter()" +
                "    .append(\"g\")\n" +
                "  .on(\"mouseover\", function(d) {\n" +
                "    d3.select(this).raise()\n" +
                "    set_highlight(d)\n" +
                "    d3.event.stopPropagation();\n" +
                "    focus_node = d;\n" +
                "    set_focus(d)\n" +
                "    if (highlight_node === null) set_highlight(d)\n" +
                "  }) \n" +
                "  .on(\"mouseout\", function(d, i) {\n" +
                "    d3.select(this).select(\"circle\")\n" +
                "      .style('stroke', 'white');\n" +
                "    hideDetail(i);\n" +
                "    exit_highlight();\n" +
                "    if (focus_node !== null) {\n" +
                "      focus_node = null;\n" +
                "      if (highlight_trans < 1) {\n" +
                "        innerCircle.style(\"opacity\", 1);\n" +
                "        centerCircle.style(\"opacity\", 1);\n" +
                "        circles.style(\"opacity\", 1);\n" +
                "        labels.style(\"opacity\", 1);\n" +
                "        moreLabels.style(\"opacity\", 1);\n" +
                "        link.style(\"opacity\", 1);\n" +
                "        linkArc.style(\"opacity\", 1);\n" +
                "        linkFeatures.style(\"opacity\", 1);\n" +
                "        mylink.style(\"opacity\", 1);\n" +
                "      }\n" +
                "    }\n" +
                "\n" +
                "    if (highlight_node === null) exit_highlight();\n" +
                "  })\n"+
                " var circles = node.append(\"circle\")\n" +
                "    .attr(\"r\", function(d) { return d.radius; })\n" +
                "    .attr(\"fill\", function(d) { return d.color; });\n" +
                "\n" +
                " var innerCircle = node.append(\"circle\")\n" +
                "    .filter(function(d) { return d.group != 1; })\n" +
                "    .attr(\"r\", function(d) { return d.radius / 1.5; })\n" +
                "    .attr(\"fill\", \"#ffffff\");\n" +
                "\n" +
                " var centerCircle = node.append(\"circle\")\n" +
                "    .filter(function(d) { return d.group == 3; })\n" +
                "    .attr(\"r\", function(d) { return d.radius / 2.5; })\n" +
                "    .attr(\"fill\", function(d) { return d.color; });\n" +
                "\n" +
                " var labels = node.append(\"text\")\n" +
                "    .attr(\"class\", \"labels\")\n" +
                "    .attr(\"font-weight\", 600)\n" +
                "    .attr(\"text-anchor\", \"middle\")\n" +
                "       .attr('y', 3)\n" +
                "       .attr('dy', -0.2);\n" +
                "    labels.text(function(d) {\n" +
                "      var doc = new DOMParser().parseFromString(d.id, \"text/html\");\n" +
                "      return doc.documentElement.textContent\n" +
                "    })\n" +
                "   .each(function(d, i) {\n" +
                "      d3.select(this).call(wrap, d.radius* 2);\n" +
                "    });\n" +
                "\n" +
                "var moreLabels = node.append(\"text\")\n" +
                "  .attr(\"class\", \"labels\")\n" +
                "  .attr(\"font-weight\", 600)\n" +
                "  .attr(\"font-family\", function(d) {\n" +
                "    return \"sans-serif\";\n" +
                "  })\n" +
                "  .attr(\"font-size\", function(d) {\n" +
                "    return 10;\n" +
                "  })\n" +
                "  .attr(\"text-anchor\", \"middle\")\n" +
                "  .attr(\"dy\", -15)\n" +
                " simulation\n" +
                "    .nodes(nodes)\n" +
                "    .on(\"tick\", ticked);\n" +
                "\n" +
                " simulation.force(\"link\")\n" +
                "    .links(links);\n" +
                "\n" +
                "function showDetail(text){\n" +
                "    androidApp.showGenreDetailInfo(text);\n" +
                " }\n" +
                "function showBundleDetail(tracks, message){\n" +
                "    androidApp.showBundleDetailInfo(tracks, message);\n" +
                " }\n" +
                "function hideDetail(text){\n" +
                "    androidApp.hideGenreDetailInfo();\n" +
                " }\n" +
                "function finishLoading(){\n" +
                "    androidApp.finishLoading();\n" +
                " }\n" +
                "function showLineDetail(text){\n" +
                "    androidApp.showLineDetail(text);\n" +
                " }\n" +
                "var nodesGroups = {};\n"
    }

    fun getTick() : String {
        return " function ticked() {\n" +
                "    link\n" +
                "       .attr(\"x1\", function(d) { return d.source.x; })\n" +
                "       .attr(\"y1\", function(d) { return d.source.y; })\n" +
                "       .attr(\"x2\", function(d) { return d.target.x; })\n" +
                "       .attr(\"y2\", function(d) { return d.target.y; });\n" +
                "\n" +
                "    linkArc.attr(\"d\", function(d) {\n" +
                "        var dx = d.target.x - d.source.x,\n" +
                "            dy = d.target.y - d.source.y,\n" +
                "            dr = Math.sqrt(dx * dx + dy * dy)*0.8;\n" +
                "        return \"M\" + \n" +
                "            d.source.x + \",\" + \n" +
                "            d.source.y + \"A\" + \n" +
                "            dr + \",\" + dr + \" 0 0,1 \" + \n" +
                "            d.target.x + \",\" + \n" +
                "            d.target.y;\n" +
                "    }); \n" +
                "    linkFeatures\n" +
                "    .attr(\"x1\", function(d) {\n" +
                "      return d.source.x + ((Math.abs(d.source.y - d.target.y) > Math.abs(d.source.x - d.target.x)) ? 10 : 0);\n" +
                "    })\n" +
                "    .attr(\"y1\", function(d) {\n" +
                "      return d.source.y + ((Math.abs(d.source.y - d.target.y) > Math.abs(d.source.x - d.target.x)) ? 0 : 10);\n" +
                "    })\n" +
                "    .attr(\"x2\", function(d) {\n" +
                "      return d.target.x + ((Math.abs(d.source.y - d.target.y) > Math.abs(d.source.x - d.target.x)) ? 10 : 0);\n" +
                "    })\n" +
                "    .attr(\"y2\", function(d) {\n" +
                "      return d.target.y + ((Math.abs(d.source.y - d.target.y) > Math.abs(d.source.x - d.target.x)) ? 0 : 10);\n" +
                "    }); \n" +
                "    node\n" +
                "       .attr(\"transform\", function(d) {\n" +
                "           return \"translate(\" + d.x + \",\" + d.y + \")\";\n" +
                "       })\n" +
                "  }\n"
    }

    fun getTickWithZoom() : String {
        return " function ticked() {\n" +
                "    link\n" +
                "       .attr(\"x1\", function(d) { return d.source.x; })\n" +
                "       .attr(\"y1\", function(d) { return d.source.y; })\n" +
                "       .attr(\"x2\", function(d) { return d.target.x; })\n" +
                "       .attr(\"y2\", function(d) { return d.target.y; });\n" +
                "\n" +
                "    linkArc.attr(\"d\", function(d) {\n" +
                "        var dx = d.target.x - d.source.x,\n" +
                "            dy = d.target.y - d.source.y,\n" +
                "            dr = Math.sqrt(dx * dx + dy * dy)*0.8;\n" +
                "        return \"M\" + \n" +
                "            d.source.x + \",\" + \n" +
                "            d.source.y + \"A\" + \n" +
                "            dr + \",\" + dr + \" 0 0,1 \" + \n" +
                "            d.target.x + \",\" + \n" +
                "            d.target.y;\n" +
                "    }); \n" +
                "    linkFeatures\n" +
                "    .attr(\"x1\", function(d) {\n" +
                "      return d.source.x + ((Math.abs(d.source.y - d.target.y) > Math.abs(d.source.x - d.target.x)) ? 10 : 0);\n" +
                "    })\n" +
                "    .attr(\"y1\", function(d) {\n" +
                "      return d.source.y + ((Math.abs(d.source.y - d.target.y) > Math.abs(d.source.x - d.target.x)) ? 0 : 10);\n" +
                "    })\n" +
                "    .attr(\"x2\", function(d) {\n" +
                "      return d.target.x + ((Math.abs(d.source.y - d.target.y) > Math.abs(d.source.x - d.target.x)) ? 10 : 0);\n" +
                "    })\n" +
                "    .attr(\"y2\", function(d) {\n" +
                "      return d.target.y + ((Math.abs(d.source.y - d.target.y) > Math.abs(d.source.x - d.target.x)) ? 0 : 10);\n" +
                "    }); \n" +
                "    node\n" +
                "       .attr(\"transform\", function(d) {\n" +
                "           return \"translate(\" + d.x + \",\" + d.y + \")\";\n" +
                "       })\n" +
                "  }\n"
    }

    fun getHighlights(): String {
        return "function set_focus(d) {\n" +
                "  circles.style(\"opacity\", function(o) {\n" +
                "    return isConnected(d, o) ? 1 : highlight_trans;\n" +
                "  });\n" +
                "  innerCircle.style(\"opacity\", function(o) {\n" +
                "    return isConnected(d, o) ? 1 : highlight_trans;\n" +
                "  });\n" +
                "  centerCircle.style(\"opacity\", function(o) {\n" +
                "    return isConnected(d, o) ? 1 : highlight_trans;\n" +
                "  });\n" +
                "  labels.style(\"opacity\", function(o) {\n" +
                "    return isConnected(d, o) ? 1 : highlight_trans;\n" +
                "  });\n" +
                "  moreLabels.style(\"opacity\", function(o) {\n" +
                "    return isConnected(d, o) ? 1 : highlight_trans;\n" +
                "  });\n" +
                "  linkArc.style(\"opacity\", function(o) {\n" +
                "    return o.source.index == d.index || o.target.index == d.index ? 1 : highlight_trans;\n" +
                "  });\n" +
                "  linkFeatures.style(\"opacity\", function(o) {\n" +
                "    return o.source.index == d.index || o.target.index == d.index ? 1 : highlight_trans;\n" +
                "  });\n" +
                "  link.style(\"opacity\", function(o) {\n" +
                "    return o.source.index == d.index || o.target.index == d.index ? 1 : highlight_trans;\n" +
                "  });\n" +
                "   mylink.style(\"opacity\", function(o) {\n" +
                "    return o.source.index == d.index || o.target.index == d.index ? 1 : highlight_trans;\n" +
                "  });\n" +
                "}\n" +
                "\n" +
                "var linkedByIndex = {};\n" +
                "\n" +
                "links.forEach(function(d) {\n" +
                "  linkedByIndex[nodes.indexOf(d.source) + \",\" + nodes.indexOf(d.target)] = true;\n" +
                "});\n" +
                "\n" +
                "function isConnected(a, b) {\n" +
                "  var isCon = linkedByIndex[a.index + \",\" + b.index] || linkedByIndex[b.index + \",\" + a.index] || a.index == b.index;\n" +
                "  if (!isCon) {\n" +
                "    isCon = bundlePairs[a.index + \",\" + b.index] || bundlePairs[b.index + \",\" + a.index]\n" +
                "  }\n" +
                "  return isCon;\n" +
                "}\n" +
                "\n" +
                "function set_highlight(d) {\n" +
                "  if (focus_node !== null) d = focus_node;\n" +
                "  highlight_node = d;\n" +
                "\n" +
                "  if (highlight_color != \"white\") {\n" +
                "    circles.style(\"stroke\", function(o) {\n" +
                "      return isConnected(d, o) ? highlight_color : \"white\";\n" +
                "    });\n" +
                "    labels.style(\"font-weight\", function(o) {\n" +
                "      return isConnected(d, o) ? \"bold\" : \"normal\";\n" +
                "    });\n" +
                "    link.style(\"stroke\", function(o) {\n" +
                "      return o.source.id == d.id || o.target.id == d.id ? highlight_color : \"#666\"\n" +
                "    });\n" +
                "    linkArc.style(\"stroke\", function(o) {\n" +
                "      return o.source.id == d.id || o.target.id == d.id ? highlight_color : \"#666\"\n" +
                "    });\n" +
                "    linkFeatures.style(\"stroke\", function(o) {\n" +
                "      return o.source.id == d.id || o.target.id == d.id ? highlight_color : \"#666\"\n" +
                "    });\n" +
                "    mylink.style(\"stroke\", function(o) {\n" +
                "      return o.source.id == d.id || o.target.id == d.id ? highlight_color : \"#666\"\n" +
                "    });\n" +
                "  }\n" +
                "}\n" +
                "\n" +
                "function exit_highlight() {\n" +
                "  highlight_node = null;\n" +
                "  if (focus_node === null) {\n" +
                "    if (highlight_color != \"white\") {\n" +
                "      circles.style(\"stroke\", \"white\");\n" +
                "      labels.style(\"font-weight\", \"normal\");\n" +
                "      link.style(\"stroke\", function(o) {\n" +
                "        highlight_color: \"#999\"\n" +
                "      });\n" +
                "      link.style(\"stroke-opacity\", function(o) {\n" +
                "        return 0.6;\n" +
                "      });\n" +
                "      linkArc.style(\"stroke\", function(o) {\n" +
                "        highlight_color: \"#999\"\n" +
                "      });\n" +
                "      linkArc.style(\"stroke-opacity\", function(o) {\n" +
                "        return 0.6;\n" +
                "      });\n" +
                "      linkFeatures.style(\"stroke\", function(o) {\n" +
                "        highlight_color: \"#999\"\n" +
                "      });\n" +
                "      linkFeatures.style(\"stroke-opacity\", function(o) {\n" +
                "        return 0.6;\n" +
                "      });\n" +
                "    }\n" +
                "\n" +
                "  }\n" +
                "}\n" +
                "\n" +
                "var focus_node = null,\n" +
                "  highlight_node = null;\n" +
                "var highlight_color = \"red\";\n" +
                "var highlight_trans = 0.1;\n" +
                "\n" +
                "d3.select(\"svg\").on(\"dblclick.zoom\", null);\n" +
                "node.on(\"dblclick.zoom\", function(d,i) {\n" +
                "  d3.event.stopPropagation();\n" +
                "   return (nodesGroups[d.id] == null) ? showDetail(i) : showBundleDetail(nodesGroups[d.id].toString(), i);\n" +
                "});"
    }

    fun getHeader() : String {
        return "<!DOCTYPE html>\n" +
                "<meta charset=\"utf-8\">\n" +
                "<style>\n" +
                "\n" +
                ".links line {\n" +
                "  stroke: #999;\n" +
                "  stroke-opacity: 0.6;\n" +
                "}\n" +
                ".mylinks line {\n" +
                "  stroke: #999;\n" +
                "  stroke-opacity: 0.6;\n" +
                "}\n" +
                "\n" +
                ".nodes circle {\n" +
                "  stroke: #fff;\n" +
                "  stroke-width: 1.5px;\n" +
                "}\n" +
                "\n" +
                "text {\n" +
                "  font-family: sans-serif;\n" +
                "  font-size: 10px;\n" +
                "}\n" +
                "span {\n" +
                "  height: 25px;\n" +
                "  width: 25px;\n" +
                "  border-radius: 50%;\n" +
                "  display : inline-block;\n" +
                "}\n" +
                "\n" +
                "</style>\n" +
                "<svg width=\"600\" height=\"600\"></svg>\n" +
                "<script src=\"https://d3js.org/d3.v4.min.js\"></script>\n" +
                "<script src=\"d3-symbol-extra.js\"></script>\n" +
                "<script>\n"
    }

    fun getFooter(): String {
        return "</script>"
    }

    fun getDrag(): String {
        return  "  // Create a drag handler and append it to the node object instead\n" +
                "  var drag_handler = d3.drag()\n" +
                "      .on(\"start\", dragstarted)\n" +
                "      .on(\"drag\", dragged)\n" +
                "      .on(\"end\", dragended);\n" +
                "\n" +
                "  drag_handler(node);\n" +
                "\n" +
                "function dragstarted(d) {\n" +
                "    if (!d3.event.active) simulation.alphaTarget(0.3).restart();\n" +
                "  d.fx = d.x;\n" +
                "  d.fy = d.y;\n" +
                "}\n" +
                "\n" +
                "function dragged(d) {\n" +
                "  d.fx = d3.event.x;\n" +
                "  d.fy = d3.event.y;\n" +
                "}\n" +
                "\n" +
                "function dragended(d) {\n" +
                "  if (!d3.event.active) simulation.alphaTarget(0);\n" +
                "  d.fx = null;\n" +
                "  d.fy = null;\n" +
                "}\n" +
                "\n"
    }

    fun getZoom(): String {
       return  "var zoom_handler = d3.zoom().scaleExtent([0.3, 5])\n" +
                "  .on(\"zoom\", zoom_actions);\n" +
                "\n" +
                "zoom_handler(svg);\n" +
               "function zoom_actions() {\n" +
               "  g.attr(\"transform\", d3.event.transform.scale(0.5))\n" +
               "  gFirst.attr(\"transform\", d3.event.transform.scale(0.5))\n" +
               "  gGenres.attr(\"transform\", d3.event.transform.scale(0.5))\n" +
               "}\n"

    }

    fun getTextWrap() : String {
        return "function wrap(text, width) {\n" +
                "  text.each(function() {\n" +
                "    var text = d3.select(this),\n" +
                "      words = text.text().split(/\\s+/).reverse(),\n" +
                "      word,\n" +
                "      mline = [],\n" +
                "      lineNumber = 0,\n" +
                "      lineHeight = 1.0, // ems\n" +
                "      y = text.attr(\"y\"),\n" +
                "      dy = parseFloat(text.attr(\"dy\")),\n" +
                "      tspan = text.text(null).append(\"tspan\").attr(\"x\", 0).attr(\"y\", y).attr(\"dy\", dy + \"em\");\n" +
                "    while (word = words.pop()) {\n" +
                "      mline.push(word);\n" +
                "      tspan.text(mline.join(\" \"));\n" +
                "      if (tspan.node().getComputedTextLength() > width || word.startsWith(\"+\")) {\n" +
                "        if (mline.length > 1) {\n" +
                "          mline.pop();\n" +
                "          tspan.text(mline.join(\" \"));\n" +
                "          mline = [word];\n" +
                "          if (/^\\+[0-9]+/.test(word)) {\n" +
                "            lineHeight = 1.5;\n" +
                "          }\n" +
                "          tspan = text.append(\"tspan\").attr(\"x\", 0).attr(\"y\", y).attr(\"dy\", ++lineNumber * lineHeight + dy + \"em\").text(word);\n" +
                "          lineHeight = 1.0\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  });\n" +
                "}"
    }

    fun getZoomFeatures(): String {
        return "var zoom_handler = d3.zoom().scaleExtent([0.3, 5])\n" +
                "  .on(\"zoom\", zoom_actions);\n" +
                "\n" +
                "zoom_handler(svg);\n" +
                "\n" +
                "var k = 1\n" +
                "var nodesCollisions = [];\n" +
                "var previousNodesGroups = {};\n" +
                "var mKeys = [];\n" +
                "var nodesDegrees = {};\n" +
                "var artLines = [];\n" +
                "var bundlePairs = {};\n" +
                "var mylink = g.append(\"g\")\n" +
                "  .attr(\"class\", \"links\")\n" +
                "  .selectAll(\"line\")\n" +
                "  .data(artLines)\n" +
                "  .enter().append(\"line\")\n" +
                "  .attr(\"stroke-width\", function(d) {\n" +
                "    return Math.sqrt(d.value * 50);\n" +
                "  });\n" +
                "nodes.forEach(function(d) {\n" +
                "  nodesDegrees[nodes.indexOf(d)] = 0;\n" +
                "});\n" +
                "links.forEach(function(d) {\n" +
                "  nodesDegrees[nodes.indexOf(d.source)] += 1;\n" +
                "  nodesDegrees[nodes.indexOf(d.target)] += 1;\n" +
                "});\n" +
                "var items = Object.keys(nodesDegrees).map(\n" +
                "  (key) => {\n" +
                "  return [key, nodesDegrees[key]]\n" +
                "  });\n" +
                "items.sort(\n" +
                "  (first, second) => {\n" +
                "    return second[1] - first[1]\n" +
                "  }\n" +
                ");\n" +
                "var keys = items.map(\n" +
                "  (e) => {\n" +
                "    return e[0]\n" +
                "  });\n" +
                "function zoom_actions() {\n" +
                "  g.attr(\"transform\", d3.event.transform.scale(0.5))\n" +
                "  gFirst.attr(\"transform\", d3.event.transform.scale(0.5))\n" +
                "  gGenres.attr(\"transform\", d3.event.transform.scale(0.5))\n" +
                "  if (k != d3.event.transform.k) {\n" +
                "    k = d3.event.transform.k;\n" +
                "    nodesCollisions = [];\n" +
                "    previousNodesGroups = nodesGroups;\n" +
                "    nodesGroups = [];\n" +
                "    mKeys = keys\n" +
                "    getCollisions()\n" +
                "    addArtificialLines()\n" +
                "    updateVisual()\n" +
                "  }\n" +
                "}\n" +
                "\n" +
                "function getCollisions() {\n" +
                "  mKeys.forEach(key => {\n" +
                "    if (!nodesCollisions.includes(parseInt(key))) {\n" +
                "      collide(nodes[key], key)\n" +
                "    }\n" +
                "  })\n" +
                "  var nodesGroupsMap = Object.keys(nodesGroups).map(\n" +
                "    (key) => {\n" +
                "      return [key, nodesGroups[key]]\n" +
                "    });\n" +
                "}\n" +
                "function getComputedRadius(d) {\n" +
                "  var sum = 0\n" +
                "  var m = nodesGroups[d.id] == null ? 0 : nodesGroups[d.id].length\n" +
                "  for (i = 0; i < m; ++i) {\n" +
                "    sum += nodes[nodesGroups[d.id][i]].radius / 8\n" +
                "  }\n" +
                "  var fullRadius = Math.min(80, d.radius + sum)\n" +
                "  return (nodesCollisions.includes(nodes.indexOf(d)) ? 0 : (Math.max(fullRadius, fullRadius * 1.5 / (k / 1.5))));\n" +
                "}\n "+
                "function updateVisual() {\n" +
                "  circles.style(\"r\", function(d) {\n" +
                "    return getComputedRadius(d);\n" +
                "  });\n" +
                "  innerCircle.style(\"r\", function(d) {\n" +
                "    return getComputedRadius(d) / 1.4\n" +
                "  });\n" +
                "  centerCircle.style(\"r\", function(d) {\n" +
                "    return getComputedRadius(d) / 1.8\n" +
                "  });\n" +
                "  labels.style(\"font-size\", function(d) {\n" +
                "    return (nodesCollisions.includes(nodes.indexOf(d)) ? 0 : (Math.max(10, 20 / (k))));\n" +
                "  })\n" +
                "  moreLabels.style(\"font-size\", function(d) {\n" +
                "    return (nodesCollisions.includes(nodes.indexOf(d)) ? 0 : (Math.max(10, 20 / (k))));\n" +
                "  })\n" +
                " moreLabels.attr(\"dy\", function(d) {\n" +
                "    return -getComputedRadius(d) / 2;\n" +
                "  })\n" +
                "\n" +
                "    moreLabels.text(function(d) {\n" +
                "      if (typeof nodesGroups[d.id] === 'function'){\n" +
                "          nodesGroups[d.id] = null\n" +
                "      }\n" +
                "      return (nodesGroups[d.id] == null) ? \"\" : \"+ \" + nodesGroups[d.id].length\n" +
                "    })\n" +
                "\n" +
                "link.style(\"stroke-width\", function(d) {\n" +
                "    return ((nodesCollisions.includes(nodes.indexOf(d.source)) || nodesCollisions.includes(nodes.indexOf(d.target)) || (nodesGroups[d.source.id] != null || nodesGroups[d.target.id] != null)) ? 0 : Math.sqrt(d.value))\n" +
                "  })\n" +
                "  linkArc.style(\"stroke-width\", function(d) {\n" +
                "    return ((nodesCollisions.includes(nodes.indexOf(d.source)) || nodesCollisions.includes(nodes.indexOf(d.target)) || (nodesGroups[d.source.id] != null || nodesGroups[d.target.id] != null)) ? 0 : Math.sqrt(d.value))\n" +
                "  })\n" +
                "  \n" +
                "  linkFeatures.style(\"stroke-width\", function(d) {\n" +
                "    return ((nodesCollisions.includes(nodes.indexOf(d.source)) || nodesCollisions.includes(nodes.indexOf(d.target)) || (nodesGroups[d.source.id] != null || nodesGroups[d.target.id] != null)) ? 0 : Math.sqrt(d.value))\n" +
                "  })\n" +
                "}\n" +
                "\n" +
                "function collide(node, key) {\n" +
                "  var i = mKeys.indexOf(key)\n" +
                "  var n = mKeys.length\n" +
                "  var dist = 0\n" +
                "  while (++i < n) {\n" +
                "    if (nodesCollisions.includes(parseInt(mKeys[i])) || nodesCollisions.includes(nodes.indexOf(node))) {\n" +
                "      continue\n" +
                "    }\n" +
                "    var first = node\n" +
                "    var second = nodes[mKeys[i]]\n" +
                "    if (first.id != second.id) {\n" +
                "      var x = first.x - second.x,\n" +
                "        y = first.y - second.y;\n" +
                "      var firstRadius = getComputedRadius(first)\n" +
                "      var secondRadius = getComputedRadius(second)\n" +
                "      var dist = firstRadius + secondRadius\n" +
                "      var collide = (Math.sqrt(x * x + y * y) < (dist))\n" +
                "      if (collide) {\n" +
                "      \t//console.log(first.id + \"--\" + second.id)\n" +
                "        nodesCollisions.push(nodes.indexOf(second))\n" +
                "        if (typeof nodesGroups[first.id] === 'function') {\n" +
                "          nodesGroups[first.id] = null\n" +
                "        }\n" +
                "        if (nodesGroups[first.id] == null) {\n" +
                "          nodesGroups[first.id] = []\n" +
                "        } \n" +
                "        if (nodesGroups[second.id] != null){\n" +
                "        \t//console.log(nodesGroups[first.id] + \"--\" + nodesGroups[second.id])\n" +
                "          var g = nodesGroups[second.id] == null ? 0 : nodesGroups[second.id].length\n" +
                "          for (t = 0; t < g; ++t) {\n" +
                "            nodesGroups[first.id].push(nodesGroups[second.id][t])\n" +
                "            nodesCollisions.push(nodesGroups[second.id])\n" +
                "          }\n" +
                "          //console.log(nodesGroups[first.id])\n" +
                "        } \n" +
                "        nodesGroups[first.id].push(nodes.indexOf(second))\n" +
                "        getCollisions()\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}"
    }

    fun getBundleLines(): String {
        return  "function addArtificialLines() {\n" +
                "  var pairs = []\n" +
                "  svg.selectAll('g.mylinks').remove();\n" +
                "  var nodesGroupsMapPairs = Object.keys(nodesGroups).map(\n" +
                "    (key) => {\n" +
                "      return {\n" +
                "        from: key,\n" +
                "        nodes: nodesGroups[key]\n" +
                "      }\n" +
                "    });\n" +
                "  var visibleNodes = JSON.parse(JSON.stringify(nodes.filter(function(el) {\n" +
                "    return !nodesCollisions.includes(nodes.indexOf(el)) &&\n" +
                "      nodesGroupsMapPairs.find(element => element.from === el.id) == undefined\n" +
                "  })));\n" +
                "  var visiblePairs = []\n" +
                "  var visibleNodesCount = visibleNodes.length\n" +
                "  for (b = 0; b < visibleNodesCount; ++b) {\n" +
                "    var mnodes = []\n" +
                "    mnodes.push(nodes.findIndex(object => {\n" +
                "      return object.id === visibleNodes[b].id;\n" +
                "    }))\n" +
                "    visiblePairs.push({\n" +
                "      from: visibleNodes[b].id,\n" +
                "      nodes: mnodes\n" +
                "    })\n" +
                "  }\n" +
                "  nodesGroupsMapPairs = nodesGroupsMapPairs.concat(visiblePairs)\n" +
                "  var m = nodesGroupsMapPairs.length\n" +
                "  for (i = 0; i < m; ++i) {\n" +
                "    for (j = i + 1; j < m; ++j) {\n" +
                "      var validLines = JSON.parse(JSON.stringify(links.filter(function(el) {\n" +
                "        return (nodesGroupsMapPairs[i].nodes.includes(nodes.indexOf(el.source)) || nodesGroupsMapPairs[i].nodes.includes(nodes.indexOf(el.target)) || nodesGroupsMapPairs[i].from === el.source.id) &&\n" +
                "          (nodesGroupsMapPairs[j].nodes.includes(nodes.indexOf(el.target)) || nodesGroupsMapPairs[j].nodes.includes(nodes.indexOf(el.source)) || nodesGroupsMapPairs[j].from === el.target.id) &&\n" +
                "          !nodesCollisions.includes(nodes.findIndex(object => {\n" +
                "            return object.id === nodesGroupsMapPairs[i].from;\n" +
                "          })) &&\n" +
                "          !nodesCollisions.includes(nodes.findIndex(object => {\n" +
                "            return object.id === nodesGroupsMapPairs[j].from;\n" +
                "          })) && (visiblePairs.filter(function(elem) {\n" +
                "          \treturn (elem.from === nodesGroupsMapPairs[i].from || elem.from === nodesGroupsMapPairs[j].from)\n" +
                "          }).length < 2)\n" +
                "      })));\n" +
                "      if (validLines.length !== 0) {\n" +
                "        pairs.push({\n" +
                "          from: nodesGroupsMapPairs[i].from,\n" +
                "          to: nodesGroupsMapPairs[j].from,\n" +
                "          lines: validLines\n" +
                "        })\n" +
                "        bundlePairs[nodes.findIndex(object => {\n" +
                "          return object.id === nodesGroupsMapPairs[i].from;\n" +
                "        }) + \",\" + nodes.findIndex(object => {\n" +
                "          return object.id === nodesGroupsMapPairs[j].from;\n" +
                "        })] = true;\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "  artLines = []\n" +
                "  var n = pairs.length\n" +
                "  for (c = 0; c < n; ++c) {\n" +
                "    pairs[c].lines[0].source = nodes.find(element => element.id === pairs[c].from);\n" +
                "    pairs[c].lines[0].target = nodes.find(element => element.id === pairs[c].to);\n" +
                "    pairs[c].lines[0].value = pairs[c].lines.length * 20\n" +
                "    artLines.push(pairs[c].lines[0])\n" +
                "  }\n" +
                "\n" +
                "  mylink = gFirst.append(\"g\")\n" +
                "    .attr(\"class\", \"mylinks\")\n" +
                "    .selectAll(\"line\")\n" +
                "    .data(artLines)\n" +
                "    .enter().append(\"line\")\n" +
                "    .on(\"mouseover\", function(d) {\n" +
                "      mylink.style(\"stroke\", function(o) {\n" +
                "        return (o === d) ? \"red\" : \"#999\"\n" +
                "      })\n" +
                "      showLineDetail(nodes.indexOf(d.source) + \",\" + nodes.indexOf(d.target) + \",\" + pairsToString((pairs.find(el => el.from === d.source.id && el.to === d.target.id)).lines) + \"BUNDLE\")\n" +
                "    })\n" +
                "    .on(\"mouseout\", function(d) {\n" +
                "      mylink.style(\"stroke\", \"#999\")\n" +
                "      hideDetail(d.source.id);\n" +
                "    })\n" +
                "    .attr(\"stroke-width\", function(d) {\n" +
                "      return Math.sqrt(d.value * 8);\n" +
                "    });\n" +
                "\n" +
                "  mylink\n" +
                "    .attr(\"x1\", function(d) {\n" +
                "      return d.source.x;\n" +
                "    })\n" +
                "    .attr(\"y1\", function(d) {\n" +
                "      return d.source.y;\n" +
                "    })\n" +
                "    .attr(\"x2\", function(d) {\n" +
                "      return d.target.x;\n" +
                "    })\n" +
                "    .attr(\"y2\", function(d) {\n" +
                "      return d.target.y;\n" +
                "    });\n" +
                "}\n" +
                "function pairsToString(pairsOfNodes) {\n" +
                "  var result = \"\"\n" +
                "  var linesCount = pairsOfNodes.length\n" +
                "  console.log(pairsOfNodes)\n" +
                "  for (s = 0; s < linesCount; ++s) {\n" +
                "    result = result + pairsOfNodes[s].source.index + \"-\" + pairsOfNodes[s].target.index + \"-\" + pairsOfNodes[s].type + \",\"\n" +
                "  }\n" +
                "  return result\n" +
                "}"
    }
}