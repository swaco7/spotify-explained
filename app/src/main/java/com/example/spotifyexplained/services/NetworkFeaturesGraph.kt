package com.example.spotifyexplained.services

object NetworkFeaturesGraph {
    fun getMainSVG(): String {
        return "var svg = d3.select(\"svg\"),\n" +
                "   width = +svg.attr(\"width\"),\n" +
                "   height = +svg.attr(\"height\");\n" +
                "\n"
    }

    fun getFeaturesSimulation(strength: Int, overlapDistFactor: Float): String {
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
                "  mKeys = keys\n" +
                "  getCollisions()\n" +
                "  updateVisual()\n" +
                "  finishLoading()\n" +
                "}\n" +
                "var overlapDistFactor = $overlapDistFactor\n"
    }

    fun addData(links: String, nodes: String): String {
        return  "var nodes = $nodes; \n" +
                "var links = $links; \n"
    }

    fun getFeaturesBody() : String {
        return " var color = d3.scaleLinear().domain([1,2])\n" +
                "    .range([\"black\", \"white\"])\n" +
                "\n" +
                " var g = svg.append(\"g\")\n" +
                "    .attr(\"class\", \"wrapper\")\n" +
                "    .attr(\"transform\", d3.zoomIdentity.translate(100, 50).scale(0.5));\n" +
                "\n" +
                " var link = g.append(\"g\")\n" +
                "    .attr(\"class\", \"links\")\n" +
                "    .selectAll(\"line\")\n" +
                "    .data(links)\n" +
                "    .enter().append(\"line\")\n" +
                "    .on(\"mouseover\", function(d) {\n" +
                "       link.style(\"stroke\", function(o){\n" +
                "           return (o === d) ? \"red\" : \"#999\"\n" +
                "       })\n" +
                "    showLineDetail(nodes.indexOf(d.source) + \",\" + nodes.indexOf(d.target) + \",\" + d.value)\n" +
                "    })\n" +
                "    .on(\"mouseout\", function(d) {\n" +
                "       link.style(\"stroke\", \"#999\")\n" +
                "       hideDetail(d.source.id);\n" +
                "    })\n" +
                "    .attr(\"stroke-width\", function(d) { return Math.sqrt(d.value); });\n" +
                "\n" +
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
                "      return d.group == 1 ? \"max \" + doc.documentElement.textContent : doc.documentElement.textContent\n"+
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
                "   moreLabels.style(\"opacity\", function(o) {\n" +
                "    return isConnected(d, o) ? 1 : highlight_trans;\n" +
                "  });\n" +
                "\n" +
                "  link.style(\"opacity\", function(o) {\n" +
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

    fun getZoom(): String {
        return  "var zoom_handler = d3.zoom()\n" +
                "  .on(\"zoom\", zoom_actions);\n" +
                "\n" +
                "zoom_handler(svg);\n" +
                "function zoom_actions() {\n" +
                "  g.attr(\"transform\", d3.event.transform.scale(0.5));" +
                "}\n"
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
                "  if (k != d3.event.transform.k) {\n" +
                "    k = d3.event.transform.k;\n" +
                "    nodesCollisions = [];\n" +
                "    previousNodesGroups = nodesGroups;\n" +
                "    nodesGroups = [];\n" +
                "    mKeys = keys\n" +
                "    getCollisions()\n" +
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
                "  link.style(\"stroke-width\", function(d) {\n" +
                "    return ((nodesCollisions.includes(nodes.indexOf(d.source)) || nodesCollisions.includes(nodes.indexOf(d.target))) ? 0 : Math.sqrt(d.value))\n" +
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
                "      var collide = (Math.sqrt(x * x + y * y) < (dist*overlapDistFactor))\n" +
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
}