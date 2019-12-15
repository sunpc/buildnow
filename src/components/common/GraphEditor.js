import $ from "jquery";
import * as d3 from "d3";
import { uuid } from "vue-uuid";
import { Message } from 'element-ui';

this.$message = Message;

let that = this;

/**
  * [refresh 兼容IE11]
  */
function refresh(link) {
  if (/(MSIE 10)|(Trident)/.test(navigator.appVersion)) {
    if (link[0] instanceof Array) {
      link[0].forEach(function(item) {
        item.parentNode.insertBefore(item, item);
      });
    } else if (link[0]) {
      var svgNode = link.node();
      svgNode.parentNode.insertBefore(svgNode, svgNode);
    }
  }
}

/**
  * [generateUUID 返回一串序列码]
  * @return {String} [uuid]
  */
function generateUUID(type) {
  return type + "-" + uuid.v1();
}

// Define GraphEditor
var GraphEditor = function(containerId, svg, nodes, edges, participants, properties) {
    var thisGraph = this;
    
    // init parameters
    thisGraph.nodes = nodes || [];
    thisGraph.edges = edges || [];
    thisGraph.participants = participants || [];
    thisGraph.properties = properties || {};

    // init job id
    if (!thisGraph.properties.chartId) {
      thisGraph.properties.chartId = generateUUID("chart");
    }

    // init container id
    thisGraph.containerId = containerId;
    
    // init state
    thisGraph.state = {
      activeEdit: true,
      selectedNode: null,
      selectedEdge: null,
      mouseDownNode: null,
      mouseDownLink: null,
      justDragged: false,
      justScaleTransGraph: false,
      lastKeyDown: -1,
      graphKeyDown: true,    // if keydown overrides, added by Simon 2018/6/14
      shiftNodeDrag: false,
      selectedText: null,
      drawLine: ""
    };

    // define arrow markers for graph links
    thisGraph.defs = svg.append("defs");
    // thisGraph.defs.append("svg:marker")
    //   .attr("id", thisGraph.containerId + "-end-arrow")
    //   .attr("viewBox", "0 -5 10 10")
    //   .attr("refX", 32)
    //   .attr("markerWidth", 5)
    //   .attr("markerHeight", 5)
    //   .attr("orient", "auto")
    //   .append("svg:path")
    //   .attr("d", "M0,-5L10,0L0,5");

    //define arrow markers for leading arrow
    thisGraph.defs.append("marker")
      .attr("id", thisGraph.containerId + "-mark-end-arrow")
      .attr("viewBox", "0 -5 10 10")
      .attr("refX", 7)
      .attr("markerWidth", 5)
      .attr("markerHeight", 5)
      .attr("orient", "auto")
      .append("svg:path")
      .attr("d", "M0,-5L10,0L0,5");

    //定义选中样式的箭头
    // thisGraph.defs.append("marker")
    //   .attr("id", thisGraph.containerId + "-selected-end-arrow")
    //   .attr("viewBox", "0 -5 10 10")
    //   .attr("refX", 30)
    //   .attr("markerWidth", 5)
    //   .attr("markerHeight", 5)
    //   .attr("orient", "auto")
    //   .append("svg:path")
    //   .attr("d", "M0,-5L10,0L0,5")
    //   .attr("fill", "rgb(229, 172, 247)");

    // define svg
    thisGraph.svg = svg;
    // thisGraph.show_position = svg.append("text").attr({
    //   "x": 800,
    //   "y": 15,
    //   "fill": "#E1784B"
    // });

    // define g
    thisGraph.svgG = svg
      .append("g")
      .classed(thisGraph.consts.graphClass, true);
    var svgG = thisGraph.svgG;

    // displayed when dragging between nodes
    thisGraph.dragLine = svgG
      .append("path")
      .attr("class", "link dragline hidden")
      .attr("d", "M0,0L0,0")
      .style("marker-end", "url(#" + thisGraph.containerId + "-mark-end-arrow)"
      );

    // svg nodes and edges
    thisGraph.paths = svgG.append("g").selectAll("g");
    thisGraph.shapes = svgG.append("g").selectAll("g");
    thisGraph.selection = svgG.append("g").selectAll("g");  // added by Simon 6/8/2018

    // handle dragging
    thisGraph.drag = d3.behavior.drag().origin(function(d) {
        // d = selected shape. The drag origin is the origin of the shape
        return {
          x: d.x,
          y: d.y
        };
      }).on("dragstart", function() {
        // d3.select(this).select("shape").attr(
        //     "r",
        //     thisGraph.consts.nodeRadius + thisGraph.consts.nodeRadiusVary
        //   );
      }).on("drag", function(args) {
        thisGraph.state.justDragged = true;
        thisGraph.dragmove.call(thisGraph, args);
      }).on("dragend", function(args) {
        // args = shape that was dragged
        // d3.select(this).select("shape").attr(
        //     "r",
        //     thisGraph.consts.nodeRadius - thisGraph.consts.nodeRadiusVary
        //   );
      });

    // handle dragging selection dots (for shapes resizing) - added by Simon 6/11/2018
    thisGraph.dragSelDot = d3.behavior.drag().origin(function(d) {
        // d = selected shape. The drag origin is the origin of the shape
        return {
          x: d.x,
          y: d.y
        };
      }).on("dragstart", function() {

      }).on("drag", function(args) {
        thisGraph.state.justDragged = true;
        thisGraph.resizemove.call(thisGraph, args);
      }).on("dragend", function(args) {

      });

    // handle zooming
    thisGraph.zoom = d3.behavior.zoom().scaleExtent([0.3, 2])
      .on("zoom", function() {
        if (d3.event.sourceEvent.shiftKey) {
          // the internal d3 state is still changing
          return false;
        } else {
          try {
            thisGraph.zoomed.call(thisGraph);
          } catch (err) {
            console.log(err);
          }
        }
        return true;
      })
      .on("zoomstart", function() {
        // console.log('zoomstart triggered');
        // var ael = d3.select("#" + thisGraph.consts.activeEditId).node();
        // if (ael) {
        //   ael.blur();
        // }
        // if (!d3.event.sourceEvent.shiftKey)
        //   d3.select("body").style("cursor", "move");
      })
      .on("zoomend", function() {
        // console.log('zoomend triggered');
        // d3.select("body").style("cursor", "auto");
      });
    svg.call(thisGraph.zoom).on("dblclick.zoom", null);

    // listen key events
    d3.select(window).on("keydown", function() {
        thisGraph.svgKeyDown.call(thisGraph);
      }).on("keyup", function() {
        thisGraph.svgKeyUp.call(thisGraph);
      });

    // listen mouse events
    svg.on("mousedown", function(d) {
      thisGraph.svgMouseDown.call(thisGraph, d);
    });

    svg.on("mouseup", function(d) {
      thisGraph.svgMouseUp.call(thisGraph, d);
    });

    // svg.on("mousemove", function(d) {
    //   thisGraph.show_position.text("" + d3.mouse(svgG.node())[0].toFixed(0) + "," + d3.mouse(svgG.node())[1].toFixed(0));
    // });

    // listen resizing
    window.onresize = function() {
      thisGraph.updateWindow(svg);
    };

    // Left-bar buttons dragging
    $("#flowComponents .components-btn[type]")
      .not(".noComponent")
      .attr("draggable", "true")
      .on("dragstart", function(ev) {
        // $('.full-left').css({cursor: 'no-drop'});
        $(this).siblings().removeClass("active").end().addClass("active");
        $(".full-right .svgbg").addClass("activate");
        /* 设置拖动过程显示图片
              var icon = document.createElement('img');
              icon.src = $(this).find('img').attr('src');
              ev.originalEvent.dataTransfer.setDragImage(icon,10,10);*/
        var json_obj = {
          text: $(this).attr("data-show"),
          component: $(this).attr("name"),
          type: $(this).attr("type"),
          category: $(this).attr("category"),
          activityType: $(this).attr("activityType"),          
          codeType: $(this).attr("codeType")
        };
        ev.originalEvent.dataTransfer.setData("tr_data", JSON.stringify(json_obj));
      })
      .on("dragend", function(ev) {
        $(".full-right .svgbg").removeClass("activate");
      });

    $(".full-right")
      .on("drop", ".svgbg", function(ev) {
        ev.stopPropagation();
        ev.preventDefault();
        var position = {
          x: parseFloat(ev.originalEvent.offsetX),
          y: parseFloat(ev.originalEvent.offsetY)
        };

        position = thisGraph.parsePosition(this, position);
        var data = JSON.parse(
          ev.originalEvent.dataTransfer.getData("tr_data")
        );
        data = $.extend(data, position);
        var node = thisGraph.createNode(data);

        thisGraph.nodes.push(node);
        thisGraph.updateGraph();
      })
      .on("dragover", function(ev) {
        ev.preventDefault();
      });
  };

  // Define consts
  GraphEditor.prototype.consts = {
    selectedClass: "selected",
    connectClass: "connect-node",
    shapeGClass: "conceptG",
    graphClass: "graph",
    activeEditId: "active-editing",
    shiftKeyRouting: "NOROUTING",   // 默认shift键连线样式
    BACKSPACE_KEY: 8,
    DELETE_KEY: 46,
    ENTER_KEY: 13,
    nodeRadius: 24,
    nodeRadiusVary: 1,
    nodeRectWidth: 120,       // 矩形宽度 added by Simon 2018/5/13
    nodeRectHeight: 60,       // 矩形高度 added by Simon 2018/5/13
    nodeColumnWidth: 120,     // 圆柱宽度 added by Simon 2018/5/29
    nodeColumnHeight: 80      // 圆柱高度 added by Simon 2018/5/29
  };

  /**
   * 获取link样式 [添加直线样式 start:连线起点 des:连线终点]
   * Added by Simon 2018/5/28
   * Redesigned by Simon 2018/7/14
   */
  GraphEditor.prototype.getLinkNoRouting = function(start, des) {
    // Calculate angle for start against des
    let xLength = Math.abs(des.x - start.x),
      yLength = Math.abs(des.y - start.y),
      zLength = Math.sqrt(Math.pow(xLength, 2) + Math.pow(yLength, 2)),
      radianStart = Math.acos(yLength / zLength),
      angleStart = Math.floor(180 / (Math.PI / radianStart));
    // Get des object
    if (d3.select("#" + des.id)[0][0]) {
      let desObj = d3.select("#" + des.id)[0][0].firstChild;
      // Calculate intersecting point
      for (let i = 0; i < desObj.getTotalLength(); i++) {
        let point = desObj.getPointAtLength(i);
        let pX = Math.floor(point.x + des.x), pY = Math.floor(point.y + des.y);
        // Calculate angle for point against des
        if (((start.x <= pX && pX <= des.x) || (start.x >= pX && pX >= des.x))
          && ((start.y <= pY && pY <= des.y) || (start.y >= pY && pY >= des.y))) {
            let xLng = Math.abs(des.x - pX),
              yLng = Math.abs(des.y - pY),
              zLng = Math.sqrt(Math.pow(xLng, 2) + Math.pow(yLng, 2)),
              radianPoint = Math.acos(yLng / zLng),
              anglePoint = Math.floor(180 / (Math.PI / radianPoint));
            if (anglePoint == angleStart || anglePoint == angleStart - 1 || anglePoint == angleStart + 1) {
              des.x = pX; des.y = pY;
              break;
            }
          }
      }
    }
    return ("M" + start.x + "," + start.y + "L" + des.x + "," + des.y);
  }

  /**
   * 获取link样式 [添加折线样式 start:连线起点 des:连线终点]
   * 如果 |dif.x| > |dif.y| 左右连线，反之，上下连线
   * 如果 dif.x > 0 && dif.y < 0 第四象限
   * 如果 dif.x > 0 && dif.y > 0 第一象限
   * 如果 dif.x < 0 && dif.y > 0 第二象限
   * 如果 dif.x < 0 && dif.y < 0 第三象限
   */
  GraphEditor.prototype.getLinkSimpleRouting = function(start, des) {
    let d = start;
    let mid_x = (d.x + des.x) / 2,
      mid_y = (d.y + des.y) / 2;
    let dif_x = des.x - d.x,
      dif_y = des.y - d.y;
    let dif_w = des.width - d.width,
      dif_h = des.height - d.height;
    let link;
    if (Math.abs(dif_x) - Math.abs(dif_w) > Math.abs(dif_y) - Math.abs(dif_h)) { // 左右连线
      if (dif_x >= 0 && dif_y >= 0) { //第一象限（200,200-300,300）
        // <path d="M 200,200 L 245,200 M 245,200 A 5,5,0,0,1 250,205 M 250,205 L 250,295 M 250,295 A 5,5,0,0,0 255,300 M 255,300 L 300,300" fill="none" stroke="#F18C16" stroke-width="1"></path>
        link = 'M' + d.x + ',' + d.y + 'L' + (mid_x) + ',' + d.y + 'M' + (mid_x) + ',' + d.y +  
          'M' + mid_x + ',' + (d.y) + 'L' + mid_x + ',' + (des.y) +'M' + mid_x + ',' + (des.y) + 
          'M' + (mid_x) + ',' + des.y + 'L' + des.x + ',' + des.y;
      }
      if (dif_x < 0 && dif_y >= 0) { //第二象限（200,200-100,300）
        // <path d="M 200,200 L 155,200 M 155,200 A 5,5,0,0,0 150,205 M 150,205 L 150,295 M 150,295 A 5,5,0,0,1 145,300 M 145,300 L 100,300" fill="none" stroke="#F18C16" stroke-width="1"></path> 
        link = 'M' + d.x + ',' + d.y + 'L' + (mid_x) + ',' + d.y + 'M' + (mid_x) + ',' + d.y + 
          'M' + mid_x + ',' + (d.y) + 'L' + mid_x + ',' + (des.y) +'M' + mid_x + ',' + (des.y) + 
          'M' + (mid_x) + ',' + des.y + 'L' + des.x + ',' + des.y;
      }
      if (dif_x < 0 && dif_y < 0) { //第三象限（200,200-100,100）
        // <path d="M 200,200 L 155,200 M 155,200 A 5,5,0,0,1 150,195 M 150,195 L 150,105 M 150,105 A 5,5,0,0,0 145,100 M 145,100 L 100,100" fill="none" stroke="#F18C16" stroke-width="1"></path>
        link = 'M' + d.x + ',' + d.y + 'L' + (mid_x) + ',' + d.y + 'M' + (mid_x) + ',' + d.y + 
          'M' + mid_x + ',' + (d.y) + 'L' + mid_x + ',' + (des.y) +'M' + mid_x + ',' + (des.y) + 
          'M' + (mid_x) + ',' + des.y + 'L' + des.x + ',' + des.y;
      }
      if (dif_x >= 0 && dif_y < 0) { //第四象限（200,200-300,100）
        // <path d="M 200,200 L 245,200 M 245,200 A 5,5,0,0,0 250,195 M 250,195 L 250,105 M 250,105 A 5,5,0,0,1 255,100 M 255,100 L 300,100" fill="none" stroke="#F18C16" stroke-width="1"></path>
        link = 'M' + d.x + ',' + d.y + 'L' + (mid_x) + ',' + d.y + 'M' + (mid_x) + ',' + d.y + 
          'M' + mid_x + ',' + (d.y) + 'L' + mid_x + ',' + (des.y) +'M' + mid_x + ',' + (des.y) + 
          'M' + (mid_x) + ',' + des.y + 'L' + des.x + ',' + des.y;
      }
    } else { // 上下连线
      if (dif_x >= 0 && dif_y >= 0) { //第一象限（200,200-300,300）
        // <path d="M 100,100 L 100,145 M 100,145 A 5,5,0,0,0 105,150 M 105,150 L 195,150 M 195,150 A 5,5,0,0,1 200,155 M 200,155 L 200,200" fill="none" stroke="#0096f2" stroke-width="1"></path>
        link = 'M' + d.x + ',' + d.y + 'L' + d.x + ',' + (mid_y) + 'M' + d.x + ',' + (mid_y) + 
          'M' + (d.x) + ',' + mid_y + 'L' + (des.x) + ',' + mid_y +'M' + (des.x) + ',' + mid_y + 
          'M' + des.x + ',' + (mid_y) + 'L' + des.x + ',' + des.y;
      }
      if (dif_x < 0 && dif_y >= 0) { //第二象限（200,200-100,300）
        // <path d="M 200,200 L 200,245 M 200,245 A 5,5,0,0,1 195,250 M 195,250 L 105,250 M 105,250 A 5,5,0,0,0 100,255 M 100,255 L 100,300" fill="none" stroke="#0096f2" stroke-width="1"></path>
        link = 'M' + d.x + ',' + d.y + 'L' + d.x + ',' + (mid_y) + 'M' + d.x + ',' + (mid_y) + 
          'M' + (d.x) + ',' + mid_y + 'L' + (des.x) + ',' + mid_y +'M' + (des.x) + ',' + mid_y + 
          'M' + des.x + ',' + (mid_y) + 'L' + des.x + ',' + des.y;
      }
      if (dif_x < 0 && dif_y < 0) { //第三象限（200,200-100,100）
        // <path d="M 200,200 L 200,155 M 200,155 A 5,5,0,0,0 195,150 M 195,150 L 105,150 M 105,150 A 5,5,0,0,1 100,145 M 100,145 L 100,100" fill="none" stroke="#0096f2" stroke-width="1"></path>
        link = 'M' + d.x + ',' + d.y + 'L' + d.x + ',' + (mid_y) + 'M' + d.x + ',' + (mid_y) + 
          'M' + (d.x) + ',' + mid_y + 'L' + (des.x) + ',' + mid_y +'M' + (des.x) + ',' + mid_y + 
          'M' + des.x + ',' + (mid_y) + 'L' + des.x + ',' + des.y;
      }
      if (dif_x >= 0 && dif_y < 0) { //第四象限（200,200-300,100）
        // <path d="M 200,200 L 200,155 M 200,155 A 5,5,0,0,1 205,150 M 205,150 L 295,150 M 295,150 A 5,5,0,0,0 300,145 M 300,145 L 300,100" fill="none" stroke="#0096f2" stroke-width="1"></path>
        link = 'M' + d.x + ',' + d.y + 'L' + d.x + ',' + (mid_y) + 'M' + d.x + ',' + (mid_y) + 
          'M' + (d.x) + ',' + mid_y + 'L' + (des.x) + ',' + mid_y +'M' + (des.x) + ',' + mid_y + 
          'M' + des.x + ',' + (mid_y) + 'L' + des.x + ',' + des.y;
      }
    }
    return link;
  };

  // /**
  //   * 获取此节点的连线
  //   * @param  {Object} node        此节点
  //   * @param  {Number} type        -1 连线指向此节点 1 此节点连出 undefined 所有连线
  //   * @return {Array}  linkedEdges 连线集合
  //   */
  GraphEditor.prototype.getLinkedEdges = function(node, type) {
    var thisGraph = this;
    var edges = thisGraph.edges;
    var linkedEdges;
    if (type == -1) {
      linkedEdges = edges.filter(function(edge) {
        return edge.target == node;
      });
    }
    if (type == 1) {
      linkedEdges = edges.filter(function(edge) {
        return edge.source == node;
      });
    }
    // linkedEdges = edges.filter(function(edge) {
    //   return edge.target == node || edge.source == node;
    // });
    return linkedEdges;
  };

  // /**
  //   * 判断node有无连线
  //   * @param  {object}  node       节点
  //   * @param  {Boolean} isActivity 是否是与activity的连线，true 不包括开始和结束节点
  //   * @param  {Boolean} type       连线类型：-1 指向node 0 所有连线 1 从node连出
  //   * @return {Boolean}            hasLinked
  //   */
  GraphEditor.prototype.hasLinked = function(node, isActivity, type) {
    var thisGraph = this;
    isActivity = isActivity || false;
    type = type || 0;
    var edges = [];
    if (isActivity) {
      edges = thisGraph.edges.filter(function(edge, index) {
        return (
          edge.source.type == "activity" && edge.target.type == "activity"
        );
      });
    } else {
      edges = thisGraph.edges;
    }
    var hasLinked = edges.some(function(edge, index) {
      if (type == 0) {
        return edge.source.id == node.id || edge.target.id == node.id;
      } else if (type == -1) {
        return edge.target.id == node.id;
      } else if (type == 1) {
        return edge.source.id == node.id;
      }
    });
    return hasLinked;
  };
  
  /**
   * PROTOTYPE FUNCTIONS 
   */
  // Drag a shape
  GraphEditor.prototype.dragmove = function(d) {
    var thisGraph = this;
    var drawLine = thisGraph.state.shiftNodeDrag ? thisGraph.consts.shiftKeyRouting : thisGraph.state.drawLine;  // by Simon 2018/5/28
    var link;
    if (thisGraph.state.shiftNodeDrag || drawLine) {
      var svgG = thisGraph.svgG, dragLine = thisGraph.dragLine;
      switch (drawLine) {
        case "NOROUTING": // 直线
          link = dragLine.attr("d", "M" + d.x + "," + d.y + "L" + d3.mouse(svgG.node())[0] + "," + d3.mouse(svgG.node())[1]);
          break;
        case "SIMPLEROUTING": // 折线
          var des = {
            x: d3.mouse(svgG.node())[0],
            y: d3.mouse(svgG.node())[1]
          };
          var link_d = thisGraph.getLinkSimpleRouting(d, des);
          link = dragLine.attr("d", link_d);
          break;
      }
      refresh(link); // 兼容IE11
    } else {
      d.x += d3.event.dx;
      d.y += d3.event.dy;
      thisGraph.updateGraph();
      /*
      // 防止shape脱出svg范围(放大缩小后还存在问题，待修改...)
      var radius = thisGraph.consts.nodeRadius + thisGraph.consts.nodeRadiusVary,
        svg_width = $('svg').width(),
        svg_heigh = $('svg').height();
      d.x = Math.max(Math.min(d3.event.x, svg_width-radius), radius);
      d.y = Math.max(Math.min(d3.event.y, svg_heigh-radius), radius);
      thisGraph.updateGraph();*/
    }
  };

  // 對shape進行resize added by Simon 6/11/2018
  GraphEditor.prototype.resizemove = function(d) {
    // TODO
    var thisGraph = this;
    if (thisGraph.state.selectedNode) {
      switch (d.cursor) {
        case "nw-resize":
          if (d3.event.dx < thisGraph.state.selectedNode.width - 10) {
            thisGraph.state.selectedNode.x += (d3.event.dx / 2);
            thisGraph.state.selectedNode.width -= d3.event.dx;
            thisGraph.state.selectedNode.outlineWidth -= d3.event.dx;
          }
          if (d3.event.dy < thisGraph.state.selectedNode.height - 10) {
            thisGraph.state.selectedNode.y += (d3.event.dy / 2);
            thisGraph.state.selectedNode.height -= d3.event.dy;
            thisGraph.state.selectedNode.outlineHeight -= d3.event.dy;
          }
          break;
        case "n-resize":
          if (d3.event.dy < thisGraph.state.selectedNode.height - 10) {
            thisGraph.state.selectedNode.y += (d3.event.dy / 2);
            thisGraph.state.selectedNode.height -= d3.event.dy;
            thisGraph.state.selectedNode.outlineHeight -= d3.event.dy;
          }
          break;
        case "ne-resize":
          if ((-1) * d3.event.dx < thisGraph.state.selectedNode.width - 10) {
            thisGraph.state.selectedNode.x += (d3.event.dx / 2);
            thisGraph.state.selectedNode.width += d3.event.dx;
            thisGraph.state.selectedNode.outlineWidth += d3.event.dx;
          }
          if (d3.event.dy < thisGraph.state.selectedNode.height - 10) {
            thisGraph.state.selectedNode.y += (d3.event.dy / 2);
            thisGraph.state.selectedNode.height -= d3.event.dy;
            thisGraph.state.selectedNode.outlineHeight -= d3.event.dy;
          }
          break;
        case "w-resize":
          if (d3.event.dx < thisGraph.state.selectedNode.width - 10) {
            thisGraph.state.selectedNode.x += (d3.event.dx / 2);
            thisGraph.state.selectedNode.width -= d3.event.dx;
            thisGraph.state.selectedNode.outlineWidth -= d3.event.dx;
          }
          break;
        case "e-resize":
          if ((-1) * d3.event.dx < thisGraph.state.selectedNode.width - 10) {
            thisGraph.state.selectedNode.x += (d3.event.dx / 2);
            thisGraph.state.selectedNode.width += d3.event.dx;
            thisGraph.state.selectedNode.outlineWidth += d3.event.dx;
          }
          break;
        case "sw-resize":
          if (d3.event.dx < thisGraph.state.selectedNode.width - 10) {
            thisGraph.state.selectedNode.x += (d3.event.dx / 2);
            thisGraph.state.selectedNode.width -= d3.event.dx;
            thisGraph.state.selectedNode.outlineWidth -= d3.event.dx;
          }
          if ((-1) * d3.event.dy < thisGraph.state.selectedNode.height - 10) {
            thisGraph.state.selectedNode.y += (d3.event.dy / 2);
            thisGraph.state.selectedNode.height += d3.event.dy;
            thisGraph.state.selectedNode.outlineHeight += d3.event.dy;
          }
          break;
        case "s-resize":
          if ((-1) * d3.event.dy < thisGraph.state.selectedNode.height - 10) {
            thisGraph.state.selectedNode.y += (d3.event.dy / 2);
            thisGraph.state.selectedNode.height += d3.event.dy;
            thisGraph.state.selectedNode.outlineHeight += d3.event.dy;
          }
          break;
        case "se-resize":
          if ((-1) * d3.event.dx < thisGraph.state.selectedNode.width - 10) {
            thisGraph.state.selectedNode.x += (d3.event.dx / 2);
            thisGraph.state.selectedNode.width += d3.event.dx;
            thisGraph.state.selectedNode.outlineWidth += d3.event.dx;
          }
          if ((-1) * d3.event.dy < thisGraph.state.selectedNode.height - 10) {
            thisGraph.state.selectedNode.y += (d3.event.dy / 2);
            thisGraph.state.selectedNode.height += d3.event.dy;
            thisGraph.state.selectedNode.outlineHeight += d3.event.dy;
          }
          break;
      }
    }
    thisGraph.updateGraph();
  };

  // Delete GRAPH
  GraphEditor.prototype.deleteGraph = function() {
    var thisGraph = this;
    thisGraph.nodes = [];
    thisGraph.edges = [];
    thisGraph.updateGraph();
  };

  /* select all text in element: taken from http://stackoverflow.com/questions/6139107/programatically-select-text-in-a-contenteditable-html-element */
  GraphEditor.prototype.selectElementContents = function(el) {
    var range = document.createRange();
    range.selectNodeContents(el);
    var sel = window.getSelection();
    sel.removeAllRanges();
    sel.addRange(range);
  };

  // remove edges associated with a node
  GraphEditor.prototype.spliceLinksForNode = function(node) {
    var thisGraph = this,
      toSplice = thisGraph.edges.filter(function(l) {
        return l.source === node || l.target === node;
      });
    toSplice.map(function(l) {
      thisGraph.edges.splice(thisGraph.edges.indexOf(l), 1);
    });
  };

  GraphEditor.prototype.replaceSelectNode = function(d3Node, nodeData) {
    // A shape node has been selected.
    var thisGraph = this;
    d3Node.classed(this.consts.selectedClass, true);
    if (thisGraph.state.selectedNode) {
      thisGraph.removeSelectFromNode();
    }
    if (thisGraph.state.selectedEdge) {
      thisGraph.removeSelectFromEdge();
    }
    thisGraph.state.selectedNode = nodeData;
    thisGraph.updateGraph();  // added by Simon 6/9/2018
  };

  GraphEditor.prototype.replaceSelectEdge = function(d3Path, edgeData) {
    var thisGraph = this;
    d3Path.classed(thisGraph.consts.selectedClass, true);
    //修改箭头样式
    // d3Path.style('marker-end', 'url(#selected-end-arrow)');
    if (thisGraph.state.selectedNode) {
      thisGraph.removeSelectFromNode();
    }
    if (thisGraph.state.selectedEdge) {
      thisGraph.removeSelectFromEdge();
    }
    thisGraph.state.selectedEdge = edgeData;
    thisGraph.updateGraph();  // added by Simon 7/10/2018
  };

  GraphEditor.prototype.removeSelectFromNode = function() {
    // A shape node has been deselected.
    var thisGraph = this;
    thisGraph.shapes.filter(function(cd) {
        return cd.id === thisGraph.state.selectedNode.id;
      }).classed(thisGraph.consts.selectedClass, false);
    thisGraph.state.selectedNode = null;
    // d3.selectAll("#inspector").remove();
  };

  GraphEditor.prototype.removeSelectFromEdge = function() {
    var thisGraph = this;
    thisGraph.paths.filter(function(cd) {
        return cd === thisGraph.state.selectedEdge;
      }).classed(thisGraph.consts.selectedClass, false);
    thisGraph.state.selectedEdge = null;
  };

  GraphEditor.prototype.pathMouseDown = function(d3path, d) {
    var thisGraph = this, state = thisGraph.state;
    d3.event.stopPropagation();
    state.mouseDownLink = d;

    // remove cell edit - added by Simon 7/9/2018
    this.closeCellEditor();

    if (state.selectedNode) {
      thisGraph.removeSelectFromNode();
    }

    var prevEdge = state.selectedEdge;
    if (!prevEdge || prevEdge !== d) {
      thisGraph.replaceSelectEdge(d3path, d);
    } else {
      if (d3.event.button != 2) {
        thisGraph.removeSelectFromEdge();
        // d.style('marker-end', 'url(#end-arrow)');
      }
    }
    // if (d3.event.button == 2) {
    //   thisGraph.showMenu();
    //   // thisGraph.menuEvent();
    // }
  };

  // mousedown on node
  GraphEditor.prototype.shapeMouseDown = function(d3node, d) {
    var thisGraph = this, state = thisGraph.state;
    d3.event.stopPropagation();
    state.mouseDownNode = d;

    // remove cell edit - added by Simon 7/9/2018
    this.closeCellEditor();

    // for linking event
    if (d3.event.shiftKey || thisGraph.state.drawLine) {
      var result = thisGraph.isAllowLinking(d);
      if (!result.success) {
        that.$message({
          type: "info",
          message: result.msg
        });
        return;
      }
      // Automatically create node when they shift + drag?
      state.shiftNodeDrag = d3.event.shiftKey;
      // reposition dragged directed edge
      var link = thisGraph.dragLine
        .classed("hidden", false)
        .attr("d", "M" + d.x + "," + d.y + "L" + d.x + "," + d.y);
      refresh(link); // 兼容IE11
      return;
    }
  };

  // mouseup on nodes
  GraphEditor.prototype.shapeMouseUp = function(d3node, d) {
    var thisGraph = this,
      state = thisGraph.state,
      consts = thisGraph.consts;
    // reset the states
    state.shiftNodeDrag = false;
    d3node.classed(consts.connectClass, false);

    var mouseDownNode = state.mouseDownNode;
    if (!mouseDownNode) return;

    thisGraph.dragLine.classed("hidden", true);

    if (mouseDownNode !== d) {
      var result = thisGraph.isAllowLinked(d, mouseDownNode);
      if (!result.success) {
        that.$message({
          type: "info",
          message: result.msg
        });
        return;
      }
      // we're in a different node: create new edge for mousedown edge and add to graph
      var newLinkType = "solid";
      if ((mouseDownNode.type == "activity" && mouseDownNode.category == "general")
        || (d.type == "activity" && d.category == "general")) {
          newLinkType = "dash";
      }
      var newEdge = {
        edgeId: generateUUID("edge"),   // seqer_edgeID.gensym(),
        source: mouseDownNode,
        target: d,
        drawLine: thisGraph.state.drawLine ? thisGraph.state.drawLine : thisGraph.consts.shiftKeyRouting,  // by Simon 2018/5/28
        linkType: newLinkType           // by Simon 2018/6/15
      };
      var filtRes = thisGraph.paths.filter(function(d) {
        if (d.source === newEdge.target && d.target === newEdge.source) {
          thisGraph.edges.splice(thisGraph.edges.indexOf(d), 1);
        }
        return d.source === newEdge.source && d.target === newEdge.target;
      });
      if (!filtRes[0].length) {
        thisGraph.edges.push(newEdge);
        thisGraph.updateGraph();
      }
    } else {
      // we're in the same node
      var prevNode = state.selectedNode;
      if (state.justDragged) {
        // dragged, not clicked
        if (state.selectedEdge) {
          thisGraph.removeSelectFromEdge();
        }
        if (!prevNode || prevNode !== d) {
          thisGraph.replaceSelectNode(d3node, d);
          // thisGraph.changePropDiv(d); // 添加更改属性div
        } else {
          // thisGraph.removeSelectFromNode();
        }
      } else {
        // clicked, not dragged
        if (d3.event.shiftKey) {
        } else {
          if (state.selectedEdge) {
            thisGraph.removeSelectFromEdge();
          }
          if (!prevNode || prevNode !== d) {
            thisGraph.replaceSelectNode(d3node, d);
            // thisGraph.changePropDiv(d); // 添加更改属性div
            // thisGraph.showMenu();
            // thisGraph.menuEvent();
          } else {
            // if (d3.event.button == "2") {
            //   // thisGraph.showMenu();
            //   // thisGraph.menuEvent();
            // } else {
            //   thisGraph.removeSelectFromNode();
            // }
          }
        }
      }
    }
    // state.mouseUpNode = state.mouseDownNode;  // added by Simon 2018/5/15 removed by Simon 2018/5/30
    state.mouseDownNode = null;
    state.justDragged = false;
    return;
  }; // end of shapes mouseup

  // 選擇點Mouse Down事件 by Simon 6/11/2018
  GraphEditor.prototype.selDotMouseDown = function(d3node, d) {
    var thisGraph = this, state = thisGraph.state;
    d3.event.stopPropagation();
    // remove cell edit - added by Simon 7/9/2018
    this.closeCellEditor();
  };

  // 選擇點Mouse Up事件 by Simon 6/11/2018
  GraphEditor.prototype.selDotMouseUp = function(d3node, d) {
    var thisGraph = this, state = thisGraph.state;
    state.justDragged = false;
  };

  // /**
  //   * 判断节点是否允许被连线
  //   * @param  {Object}  mouseDownNode 连线开始节点
  //   * @param  {Object}  eventNode     连线结束节点
  //   * @return {Object}                连线是否成功信息
  //   */
  GraphEditor.prototype.isAllowLinked = function(eventNode, mouseDownNode) {
    var thisGraph = this;
    var result = {
      success: true,
      msg: ""
    };
    switch (eventNode.type) {
      case "start":
        result.success = false;
        result.msg = "Not allowed!";
        break;
      case "end":
        if (thisGraph.hasLinked(eventNode)) {
          result.success = false;
          result.msg = "Already linked!";
        }
        break;
    }
    switch (mouseDownNode.type) {
      case "start":
        if (thisGraph.hasLinked(mouseDownNode)) {
          result.success = false;
          result.msg = "Already linked!";
        }
        break;
      case "end":
        result.success = false;
        result.msg = "Not allowed!";
        break;
      case "activity":
        // var edges = thisGraph.getLinkedEdges(mouseDownNode, 1);
        // var edgeLinkEnd = edges.filter(function(edge) {
        //   return edge.target.type == "end";
        // });
        // if (edgeLinkEnd.length) {
        //   result.success = false;
        //   result.msg = "活动不能有转出转移！";
        // }
        break;
    }
    return result;
  };

  // /**
  // * 判断节点是否允许连线
  // * @param  {Object}  eventNode 出发实践节点对象
  // * @return {Object}            连线是否成功信息
  // */
  GraphEditor.prototype.isAllowLinking = function(eventNode) {
    var thisGraph = this;
    var result = {
      success: true,
      msg: ""
    };
    switch (eventNode.type) {
      case "start":
        if (thisGraph.hasLinked(eventNode)) {
          result.success = false;
          result.msg = "Already linked!";
        }
        break;
      case "end":
        result.success = false;
        result.msg = "Not allowed!";
        break;
      case "activity":
        // var edges = thisGraph.getLinkedEdges(eventNode, 1);
        // var edgeLinkEnd = edges.filter(function(edge) {
        //   return edge.target.type === "end";
        // });
        // if (edgeLinkEnd.length) {
        //   result.success = false;
        //   result.msg = "活动不能有转出转移！";
        // }
        break;
    }
    return result;
  };

  GraphEditor.prototype.updateWindow = function(svg) {
    var docEl = document.documentElement,
      bodyEl = document.getElementsByTagName("body")[0];
    var x = window.innerWidth || docEl.clientWidth || bodyEl.clientWidth;
    var y = window.innerHeight || docEl.clientHeight || bodyEl.clientHeight;
    svg.attr("width", x).attr("height", y);
  };

  /**
   * 创建子流程graph对象
   */
  // GraphEditor.prototype.createSubGraph = function() {
  //   var thisGraph = this;
  //   var containerId = d3.select(".full-right").attr("data-tab");
  //   // activitySetId = d3.select('.full-right>.menu>.item.active').attr('activitysetid');

  //   var svg = d3
  //     .select(".full-right .svgbg")
  //     .append("svg")
  //     .attr("width", "100%")
  //     .attr("height", "100%");
  //   /*
  //             var editedBlockNode = graph_main.nodes.find(function(node) {
  //               return (node.component == 'blockActivity' && node.activitySet.activitySetId == activitySetId);
  //             });*/
  //   var editedBlockNode = thisGraph.state.selectedNode;

  //   var subGraph = editedBlockNode.activitySet.GraphEditor;
  //   var pools = that.graphPool.pools;
  //   var isExist = pools.indexOf(subGraph);
  //   if (isExist === -1) {
  //     var graph_active;
  //     if (subGraph) {
  //       graph_active = new GraphEditor(
  //         containerId,
  //         svg,
  //         subGraph.nodes,
  //         subGraph.edges,
  //         subGraph.participants
  //       );
  //     } else {
  //       graph_active = new GraphEditor(containerId, svg, [], [], []);
  //       editedBlockNode.activitySet.GraphEditor = graph_active;
  //     }
  //     pools.push(graph_active);
  //     that.graphPool.updateGraphActiveById(containerId);
  //     graph_active.updateGraph();
  //   }
  // };

  GraphEditor.prototype.createNode = function(data) {
    var node;
    if (data.type == "activity") {
      // common attributes
      node = {
        id: generateUUID("node"),   // seqer_nodeID.gensym(),
        name: data.text,
        desc: "",
        longDesc: "",   // added by Simon 9/1/2018 
        // component: data.component,
        type: data.type,
        category: data.category,
        activityType: data.activityType,
        codeType: data.codeType,
        x: data.x,
        y: data.y,
        fillColor: "#fff"
      };
      // if (data.component == "blockActivity") {
      //   node.activitySet = {
      //     activitySetId: seqer_blockId.gensym(),
      //     GraphEditor: null
      //   };
      // }
      // Added by Simon 5/18/2018
      switch (data.activityType) {
        case "FILE":
          // Define width and height     
          node.width = this.consts.nodeRectWidth;
          node.height = this.consts.nodeRectHeight;
          node.outlineWidth = node.width + 40;
          node.outlineHeight = node.height;
          // Define fillColor
          node.fillColor = "#E6E188";
          // Add default properties      
          node.dcb = {
            "recfm": "FB",
            "lrecl": "0"
          };
          node.space = "SPCM";
          node.unit = "UNIT";
          node.disp = {
            pri: "NEW",
            sec: "CATLG",
            thd: "DELETE"
          };
          node.layout = {
            "name": "",
            "data": []
          };
          break;
        case "TABLE":
          // Define width and height     
          node.width = this.consts.nodeColumnWidth;
          node.height = this.consts.nodeColumnHeight;
          node.outlineWidth = node.width;
          node.outlineHeight = node.height;
          // Define fillColor
          node.fillColor = "#f2dbdb";
          break;
        case "PROC":
          // Define width and height     
          node.width = this.consts.nodeRectWidth;
          node.height = this.consts.nodeRectHeight;
          node.outlineWidth = node.width;
          node.outlineHeight = node.height;
          // Define fillColor
          node.fillColor = "#eaf1de";
          // Add parameters
          node.parameters = [];
          break;
        case "SORT":
          // Define width and height     
          node.width = this.consts.nodeRectWidth;
          node.height = this.consts.nodeRectHeight;
          node.outlineWidth = node.width;
          node.outlineHeight = node.height;
          // Define fillColor
          node.fillColor = "#9AF29B";
          break;        
        case "JOIN":
          // Define width and height     
          node.width = this.consts.nodeRectWidth;
          node.height = this.consts.nodeRectHeight;
          node.outlineWidth = node.width;
          node.outlineHeight = node.height;
          // Define fillColor
          node.fillColor = "#d9eef3";
          break;        
        case "SPLIT":
          // Define width and height     
          node.width = this.consts.nodeRectWidth;
          node.height = this.consts.nodeRectHeight;
          node.outlineWidth = node.width;
          node.outlineHeight = node.height;
          // Define fillColor
          node.fillColor = "#eaf1de";
          break;
        case "UNLOAD":
          // Define width and height     
          node.width = this.consts.nodeRectWidth;
          node.height = this.consts.nodeRectHeight;
          node.outlineWidth = node.width + 20;
          node.outlineHeight = node.height;
          // Define fillColor
          node.fillColor = "#eaf1de";
          break;
        case "LOAD":
          // Define width and height     
          node.width = this.consts.nodeRectWidth;
          node.height = this.consts.nodeRectHeight;
          node.outlineWidth = node.width + 20;
          node.outlineHeight = node.height;
          // Define fillColor
          node.fillColor = "#eaf1de";
          break;  
        case "SQL":
          // Define width and height     
          node.width = this.consts.nodeRectWidth;
          node.height = this.consts.nodeRectHeight;
          node.outlineWidth = node.width;
          node.outlineHeight = node.height;
          // Define fillColor
          node.fillColor = "#eaf1de";
          break;
      }
    } else {
      // 开始、结束节点
      node = {
        id: generateUUID("node"),
        name: data.text,
        // component: data.component,
        type: data.type,
        x: data.x,
        y: data.y,
        radius: this.consts.nodeRadius    // added by Simon 2018/5/13
      };
    }
    return node;
  };
  
  // mouseup on main svg
  GraphEditor.prototype.svgMouseUp = function() {
    var thisGraph = this,
      state = thisGraph.state;
    if (state.justScaleTransGraph) {
      // dragged not clicked
      state.justScaleTransGraph = false;
    // } else if (state.graphMouseDown && d3.event.shiftKey) {
    //   // clicked not dragged from svg
    //   var xycoords = d3.mouse(thisGraph.svgG.node()),
    //     d = {
    //       id: generateUUID(),   // seqer_nodeID.gensym(),
    //       name: "普通活动",
    //       component: "ordinaryActivity",
    //       type: "activity",
    //       x: xycoords[0],
    //       y: xycoords[1],
    //       monitorinf: { isResponsibleTem: true },
    //       eventTypeId: null
    //     };
    //   thisGraph.nodes.push(d);
    //   thisGraph.updateGraph();
    } else if (state.shiftNodeDrag || state.drawLine) {
      // dragged from node
      state.shiftNodeDrag = false;
      thisGraph.dragLine.classed("hidden", true); //win7 IE11下存在bug
    }
    state.graphMouseDown = false;
  };

  // keydown on main svg
  GraphEditor.prototype.svgKeyDown = function() {
    var thisGraph = this,
      state = thisGraph.state,
      consts = thisGraph.consts;
    // if not graphKeyDown, return directly.
    if (!state.graphKeyDown) {
      return;
    }
    // make sure repeated key presses don't register for each keydown
    if (state.lastKeyDown !== -1) return;
    state.lastKeyDown = d3.event.keyCode;
    var selectedNode = state.selectedNode,
      selectedEdge = state.selectedEdge;
    switch (d3.event.keyCode) {
      case consts.ENTER_KEY:
        d3.event.preventDefault();
        break;
      case consts.BACKSPACE_KEY:
      case consts.DELETE_KEY:
        d3.event.preventDefault();
        if (selectedNode) {
          thisGraph.nodes.splice(thisGraph.nodes.indexOf(selectedNode), 1);
          thisGraph.spliceLinksForNode(selectedNode);
          state.selectedNode = null;
          thisGraph.updateGraph();
          // thisGraph.
        } else if (selectedEdge) {
          thisGraph.edges.splice(thisGraph.edges.indexOf(selectedEdge), 1);
          state.selectedEdge = null;
          thisGraph.updateGraph();
        }
        break;
    }
  };

  GraphEditor.prototype.svgKeyUp = function() {
    this.state.lastKeyDown = -1;
  };

  // close cell editor // by Simon 7/9/2018
  GraphEditor.prototype.closeCellEditor = function() {
    // enable key down
    this.state.graphKeyDown = true;
    // get svgbg div
    var svgBgDiv = document.getElementById("svgbg");
    // close name editor
    var nameEditorDiv = document.getElementById("nameEditor");
    if (nameEditorDiv) {
      $("foreignObject").each(function() {
        this.style.visibility = "visible";
      });
      if (this.state.selectedNode) {
        this.state.selectedNode.name = nameEditorDiv.innerText;
        this.updateGraph();
      }
      svgBgDiv.removeChild(nameEditorDiv);
    }
    // close desc editor
    var descEditorDiv = document.getElementById("descEditor");
    if (descEditorDiv) {
      $("foreignObject").each(function() {
        this.style.visibility = "visible";
      });
      if (this.state.selectedNode) {
        this.state.selectedNode.desc = descEditorDiv.innerText;
        this.updateGraph();
      }
      svgBgDiv.removeChild(descEditorDiv);
    }
  };
  
  // mousedown on main svg
  GraphEditor.prototype.svgMouseDown = function() {
    this.state.graphMouseDown = true;
    // remove cell edit - added by Simon 7/9/2018
    this.closeCellEditor();
    // remove selected node and edge - added by Simon 2018/5/30
    if (this.state.selectedNode) {
      this.removeSelectFromNode();
      this.updateGraph();  // added by Simon 6/9/2018
    }
    if (this.state.selectedEdge) {
      this.removeSelectFromEdge();
    }
  };

  // add marker-end // added by Simon 2018/5/13
  GraphEditor.prototype.addMarkerEnd = function(d) {
    var thisGraph = this;
    if (d.target.type == 'end') {
      var markerID = generateUUID("marker") + "-end-arrow", refX = 0;
      if (d.drawLine == "NOROUTING") {  
        refX = 10;
      } else if (d.drawLine == "SIMPLEROUTING") {
        refX = 32;
      }
      // define the marker
      thisGraph.defs.append("svg:marker")
        .attr("id", markerID)
        .attr("viewBox", "0 -5 10 10")
        .attr("refX", refX) // was 42
        .attr("markerWidth", 5)
        .attr("markerHeight", 5)
        .attr("orient", "auto")
        .append("svg:path")
        .attr("d", "M0,-5L10,0L0,5");
      // return the url   
      // return "url(#" + thisGraph.containerId + "-end-arrow)";
      return "url(#" + markerID + ")";
    } else {
      var markerID = generateUUID("marker") + "-end-arrow", refX = 0;
      if (d.drawLine == "NOROUTING") {
        refX = 10;
      } else if (d.drawLine == "SIMPLEROUTING") {
        var dif_x = d.target.x - d.source.x, 
          dif_y = d.target.y - d.source.y;
        var dif_w = (d.target.width || d.target.radius) - (d.source.width || d.source.radius),
          dif_h = (d.target.height || d.target.radius) - (d.source.height || d.source.radius);
        if (Math.abs(dif_x) - Math.abs(dif_w) > Math.abs(dif_y) - Math.abs(dif_h)) { // 左右连线
          refX = d.target.width / 2 + 10;
        } else {  // 上下连线
          refX = d.target.height / 2 + 10;
        }  
      }      
      // define the marker
      thisGraph.defs.append("svg:marker")
        .attr("id", markerID)
        .attr("viewBox", "0 -5 10 10")
        .attr("refX", refX) // was 42
        .attr("markerWidth", 5)
        .attr("markerHeight", 5)
        .attr("orient", "auto")
        .append("svg:path")
        .attr("d", "M0,-5L10,0L0,5");
      // return the url    
      return "url(#" + markerID + ")";
    }
  }

  // draw shapes for each activity type // added by Simon 6/11/2018
  GraphEditor.prototype.drawShape = function(gEl, d, isNew) {
    switch (d.activityType) {
      case "FILE":
        var points = (d.width / 2 * (-1) + 20)
          + "," + (d.height / 2 * (-1))
          + " " + (d.width / 2 + 20)
          + "," + (d.height / 2 * (-1))
          + " " + (d.width / 2 - 20)
          + "," + (d.height / 2)
          + " " + (d.width / 2 * (-1) - 20)
          + "," + (d.height / 2);
        if (isNew) gEl.append("polygon");
        gEl.select("polygon").attr("points", points).attr("style", "fill:" + d.fillColor);
        break;
      case "TABLE":
        // Re-drawn by Simon 7/16/2018
        if (isNew) gEl.append("path").attr("id", "path1");
        gEl.select("#path1")
          .attr("d", "M" + d.width * (-0.5) + "," + d.height * (-0.3)
            + "C" + d.width * (-0.5) + "," + d.height * (-0.5667) + "," + (d.width * 0.5) + "," + d.height * (-0.5667) + "," + (d.width * 0.5) + "," + d.height * (-0.3) 
            + "L" + (d.width * 0.5) + "," + d.height * (0.3) + " "
            + "C" + (d.width * 0.5) + "," + d.height * (0.5667) + "," + d.width * (-0.5) + "," + d.height * (0.5667) + "," + d.width * (-0.5) + "," + d.height * (0.3)
            + "Z")
          .attr("fill", d.fillColor);
        if (isNew) gEl.append("path").attr("id", "path2");
        gEl.select("#path2")
          .attr("d", "M" + d.width * (-0.5) + "," + d.height * (-0.3)
            + "C" + d.width * (-0.5) + "," + d.height * (-0.0333) + "," + (d.width * 0.5) + "," + d.height * (-0.0333) + "," + (d.width * 0.5) + "," + d.height * (-0.3))
          .attr("fill", "none");
        break;
      case "PROC":
        if (isNew) gEl.append("rect");
        gEl.select("rect")
          .attr("x", d.width / 2 * (-1))
          .attr("y", d.height / 2 * (-1))
          .attr("width", d.width)
          .attr("height", d.height)
          .attr("style", "fill:" + d.fillColor);
        if (isNew) gEl.append("line").attr("id", "line1");
        gEl.select("#line1")
          .attr("x1", d.width / 2 * (-1) + 10)
          .attr("y1", d.height / 2 * (-1) + 15)
          .attr("x2", d.width / 2 * (-1) + 20)
          .attr("y2", d.height / 2 * (-1) + 15);
        if (isNew) gEl.append("line").attr("id", "line2");
        gEl.select("#line2")
          .attr("x1", d.width / 2 * (-1) + 15)
          .attr("y1", d.height / 2 * (-1) + 10)
          .attr("x2", d.width / 2 * (-1) + 20)
          .attr("y2", d.height / 2 * (-1) + 15);
        if (isNew) gEl.append("line").attr("id", "line3");
        gEl.select("#line3")
          .attr("x1", d.width / 2 * (-1) + 15)
          .attr("y1", d.height / 2 * (-1) + 20)
          .attr("x2", d.width / 2 * (-1) + 20)
          .attr("y2", d.height / 2 * (-1) + 15);
        break;
      case "SORT":
        if (isNew) gEl.append("rect");
        gEl.select("rect")
          .attr("x", d.width / 2 * (-1))
          .attr("y", d.height / 2 * (-1))
          .attr("width", d.width)
          .attr("height", d.height)
          .attr("style", "fill:" + d.fillColor);
        break;
      case "JOIN":
        if (isNew) gEl.append("rect");
        gEl.select("rect")
          .attr("x", d.width / 2 * (-1))
          .attr("y", d.height / 2 * (-1))
          .attr("width", d.width)
          .attr("height", d.height)
          .attr("style", "fill:" + d.fillColor);
        if (isNew) gEl.append("line").attr("id", "line1");
        gEl.select("#line1")
          .attr("x1", d.width / 2 * (-1) + 10)
          .attr("y1", d.height / 2 * (-1))
          .attr("x2", d.width / 2 * (-1) + 10)
          .attr("y2", d.height / 2);
        if (isNew) gEl.append("line").attr("id", "line2");
        gEl.select("#line2")
          .attr("x1", d.width / 2 - 10)
          .attr("y1", d.height / 2 * (-1))
          .attr("x2", d.width / 2 - 10)
          .attr("y2", d.height / 2);
        break;
      case "SPLIT":
        if (isNew) gEl.append("rect");
        gEl.select("rect")
          .attr("x", d.width / 2 * (-1))
          .attr("y", d.height / 2 * (-1))
          .attr("width", d.width)
          .attr("height", d.height)
          .attr("style", "fill:" + d.fillColor);
        if (isNew) gEl.append("line").attr("id", "line1");
        gEl.select("#line1")
          .attr("x1", d.width / 2 * (-1) + 10)
          .attr("y1", d.height / 2 * (-1))
          .attr("x2", d.width / 2 * (-1) + 10)
          .attr("y2", -5);
        if (isNew) gEl.append("line").attr("id", "line2");
        gEl.select("#line2")
          .attr("x1", d.width / 2 * (-1) + 10)
          .attr("y1", 5)
          .attr("x2", d.width / 2 * (-1) + 10)
          .attr("y2", d.height / 2);
        if (isNew) gEl.append("line").attr("id", "line3");
        gEl.select("#line3")
          .attr("x1", d.width / 2 - 10)
          .attr("y1", d.height / 2 * (-1))
          .attr("x2", d.width / 2 - 10)
          .attr("y2", -5);
        if (isNew) gEl.append("line").attr("id", "line4");
        gEl.select("#line4")
          .attr("x1", d.width / 2 - 10)
          .attr("y1", 5)
          .attr("x2", d.width / 2 - 10)
          .attr("y2", d.height / 2);
        break;
      case "UNLOAD":
        var points = (d.width / 2 * (-1) + 10)
          + "," + (d.height / 2 * (-1))
          + " " + (d.width / 2 - 10)
          + "," + (d.height / 2 * (-1))
          + " " + (d.width / 2 + 10)
          + "," + (d.height / 2)
          + " " + (d.width / 2 * (-1) - 10)
          + "," + (d.height / 2);
        if (isNew) gEl.append("polygon");
        gEl.select("polygon").attr("points", points).attr("style", "fill:" + d.fillColor);
        break;
      case "LOAD":
        var points = (d.width / 2 * (-1) - 10)
          + "," + (d.height / 2 * (-1))
          + " " + (d.width / 2 + 10)
          + "," + (d.height / 2 * (-1))
          + " " + (d.width / 2 - 10)
          + "," + (d.height / 2)
          + " " + (d.width / 2 * (-1) + 10)
          + "," + (d.height / 2);
        if (isNew) gEl.append("polygon");
        gEl.select("polygon").attr("points", points).attr("style", "fill:" + d.fillColor);
        break;
      case "SQL":
        if (isNew) gEl.append("rect");
        gEl.select("rect")
          .attr("x", d.width / 2 * (-1))
          .attr("y", d.height / 2 * (-1))
          .attr("width", d.width)
          .attr("height", d.height)
          .attr("style", "fill:" + d.fillColor);
        if (isNew) gEl.append("text").attr("text-anchor", "middle").attr("letter-spacing", "0").attr("id", "text1");
        gEl.select("#text1")
          .append("tspan").text("SQL")
          .attr("x", d.width / 2 * (-1) + 15)
          .attr("y", d.height / 2 * (-1) + 15)
          .attr("style", "font-weight: bold; font-style:italic; fill: #C1C1C1");
        break;
    }
  }

  // call to propagate changes to graph
  GraphEditor.prototype.updateGraph = function() {
    // console.log("DEBUG: updateGraph");
    var thisGraph = this,
      consts = thisGraph.consts,
      state = thisGraph.state,
      nodes = thisGraph.nodes,
      edges = thisGraph.edges;
    
    // fullfil the node properties // added by Simon 7/4/2018
    nodes.filter(function(selectedNode) {
      return selectedNode.type == "activity";
    }).forEach(function(selectedNode) {
      switch (selectedNode.activityType) {
        case "FILE":
          break;
        case "TABLE":
          // populate the input tables
          if (thisGraph.hasLinked(selectedNode, true, 1)) {
            var edges = thisGraph.edges.filter(function(edge, index) {
              return (
                edge.target.type == "activity" && edge.source.id == selectedNode.id
              );
            });
            // handle each output
            edges.forEach(function(edge, index) {
              edge.target.refInputTables = selectedNode.tables;
            });
          }
          // populate the output tables
          if (thisGraph.hasLinked(selectedNode, true, -1)) {
            var edges = thisGraph.edges.filter(function(edge, index) {
              return (
                edge.target.type == "activity" && edge.target.id == selectedNode.id
              );
            });
            // handle each output
            edges.forEach(function(edge, index) {
              edge.source.refOutputTables = selectedNode.tables;
            });
          }
          break;
        case "PROC":
          break;
        case "SORT":
          // populate the input layout if linked
          if (thisGraph.hasLinked(selectedNode, true, -1)) {
            var edges = thisGraph.edges.filter(function(edge, index) {
              return (
                edge.source.type == "activity" && edge.source.activityType == "FILE" && edge.target.id == selectedNode.id
              );
            });
            // handle each input
            edges.forEach(function(edge, index) {
              // add to run.sortin
              let alreadySortin = false;
              for (let idx in selectedNode.run.sortin) {
                if (selectedNode.run.sortin[idx].refId == edge.source.id) {
                  selectedNode.run.sortin[idx].name = edge.source.name;
                  alreadySortin = true;
                  break;
                }
              }
              if (!alreadySortin) {
                selectedNode.run.sortin.push({
                  "id": index,
                  "refId": edge.source.id,
                  "name": edge.source.name
                });
              }
              // push into input[]
              if (!selectedNode.input[index] || selectedNode.input[index].refId != edge.source.id) {
                selectedNode.input[index] = {
                  "id": index,
                  "refId": edge.source.id,
                  "name": edge.source.name,
                  "layout": edge.source.layout,
                  "include": []
                };
              } else {
                selectedNode.input[index].name = edge.source.name;
                selectedNode.input[index].layout = edge.source.layout;
              }
            });
            // init input reformat
            if (edges.length > 0 && selectedNode.inrec.length == 0) {
              selectedNode.inrec = [];
              selectedNode.input[0].layout.data.forEach(function(item, index) {
                selectedNode.inrec.push({
                  "name": item.name,
                  "type": item.type,
                  "length": item.length,
                  "mapping": item.name
                });
              });
            }
          }
          // populate the output layout if linked
          if (thisGraph.hasLinked(selectedNode, true, 1)) {
            var edges = thisGraph.edges.filter(function(edge, index) {
              return (
                edge.target.type == "activity" && edge.target.activityType == "FILE" && edge.source.id == selectedNode.id
              );
            });
            // handle each output
            edges.forEach(function(edge, index) {
              // sync output
              if (!selectedNode.output[index] || selectedNode.output[index].refId != edge.target.id) {
                selectedNode.output[index] = {
                  "id": index,
                  "refId": edge.target.id,
                  "name": edge.target.name,
                  "include": []
                };
              } else {
                selectedNode.output[index].name = edge.target.name;
              }
              // if target layout exists, get the layout from the target node, else get from input.
              if (edge.target.layout.data.length > 0) {
                selectedNode.output[index].layout = edge.target.layout;
              } else {
                selectedNode.output[index].layout = JSON.parse(JSON.stringify(selectedNode.input[0].layout));
                edge.target.layout = selectedNode.output[index].layout;
              } 
              // set default mappings
              selectedNode.output[index].layout.data.forEach(function(item, index) {
                if (!item.mapping) {
                  item.mapping = item.name;
                }
              });
            });
          }
          break;
        case "JOIN":
          // populate the input layout if linked
          if (thisGraph.hasLinked(selectedNode, true, -1)) {
            var edges = thisGraph.edges.filter(function(edge, index) {
              return (
                edge.source.type == "activity" && edge.source.activityType == "FILE" && edge.target.id == selectedNode.id
              );
            });
            if (edges.length > 0 && selectedNode.input[0].layout.name != "SJN" + edges[0].source.layout.name.substr(3)) {
              selectedNode.input[0].name = edges[0].source.name;
              selectedNode.input[0].layout = JSON.parse(JSON.stringify(edges[0].source.layout));
              selectedNode.input[0].layout.name = "SJN" + edges[0].source.layout.name.substr(3);
              selectedNode.input[0].layout.data.forEach(function(item, index) {
                if (item.name && item.name != "") {
                  item.name = "FILE1_" + item.name;
                  item.joinkey = false;
                }
              });
            }
            if (edges.length > 1 && selectedNode.input[1].layout.name != "SJN" + edges[1].source.layout.name.substr(3)) {
              selectedNode.input[1].name = edges[1].source.name;
              selectedNode.input[1].layout = JSON.parse(JSON.stringify(edges[1].source.layout));
              selectedNode.input[1].layout.name = "SJN" + edges[1].source.layout.name.substr(3);
              selectedNode.input[1].layout.data.forEach(function(item, index) {
                if (item.name && item.name != "") {
                  item.name = "FILE2_" + item.name;
                  item.joinkey = false;
                }
              });
            }
          }
          // populate the output layout if linked
          if (thisGraph.hasLinked(selectedNode, true, 1)) {
            var edges = thisGraph.edges.filter(function(edge, index) {
              return (
                edge.target.type == "activity" && edge.target.activityType == "FILE" && edge.source.id == selectedNode.id
              );
            });
            // handle each output
            edges.forEach(function(edge, index) {
              if (!selectedNode.output[index] || selectedNode.output[index].refId != edge.target.id) {
                selectedNode.output[index] = {
                  "id": index,
                  "refId": edge.target.id,
                  "name": edge.target.name,
                  "layout": {
                    "name": "",
                    "data": []
                  },
                  "include": []
                };
              }
              // if target layout exists, get the layout from the target node, else get from input.
              if (edge.target.layout.data.length > 0) {
                selectedNode.output[index].layout = edge.target.layout;
              } else {
                if (selectedNode.input.length > 1
                  && selectedNode.input[0].layout.data.length > 0
                  && selectedNode.input[1].layout.data.length > 0) {
                  selectedNode.output[index].layout.name = "SYMJNOUT";
                  selectedNode.output[index].layout.data = [];
                  for (var i in selectedNode.input[0].layout.data) {
                    if (selectedNode.input[0].layout.data[i] && selectedNode.input[0].layout.data[i].name) {
                      selectedNode.output[index].layout.data.push(JSON.parse(JSON.stringify(selectedNode.input[0].layout.data[i])));
                    }
                  }
                  for (var i in selectedNode.input[1].layout.data) {
                    if (selectedNode.input[1].layout.data[i] && selectedNode.input[1].layout.data[i].name) {
                      selectedNode.output[index].layout.data.push(JSON.parse(JSON.stringify(selectedNode.input[1].layout.data[i])));
                    }
                  }                  
                  // set join file marker
                  selectedNode.output[index].layout.data.push({
                    "name": "JOIN_MARKER",
                    "type": "CH",
                    "length": 1
                  });
                  edge.target.layout = selectedNode.output[index].layout;
                }
              } 
              // set default mappings --- no longer needed --- by Simon 9/13/2018
              // selectedNode.output[index].layout.data.forEach(function(item, index) {
              //   if (!item.mapping) {
              //     item.mapping = item.name;
              //   }
              // });
            });
          }
          break;          
        case "SPLIT":
          // populate the input layout if linked
          if (thisGraph.hasLinked(selectedNode, true, -1)) {
            var edges = thisGraph.edges.filter(function(edge, index) {
              return (
                edge.source.type == "activity" && edge.source.activityType == "FILE" && edge.target.id == selectedNode.id
              );
            });
            // handle input
            if (edges.length > 0) {
              selectedNode.split.refInputColumns = edges[0].source.layout.data;
            }
          }
          // populate the output layout if linked
          if (thisGraph.hasLinked(selectedNode, true, 1)) {
            var edges = thisGraph.edges.filter(function(edge, index) {
              return (
                edge.target.type == "activity" && edge.target.activityType == "FILE" && edge.source.id == selectedNode.id
              );
            });
            // handle input
            if (edges.length > 0) {
              selectedNode.split.refOutputName = edges[0].target.name;
            }
          }
          break;
        case "UNLOAD":
          break;
        case "LOAD":
          break;         
        case "SQL":
          break;
      }
    });

    // update existing shapes
    thisGraph.shapes = thisGraph.shapes.data(nodes, function(d) {
      return d.id;
    });
    
    thisGraph.shapes.attr("transform", function(d) {
      if (d == state.selectedNode) {
        // 更新节点形状 added by Simon 2018/6/11
        thisGraph.drawShape(d3.select(this), d);
        // 更新节点名称 added by Simon 2018/9/2
        // var tspan_name = d3.select(this).select("#tspan_name");
        // if (d.category == "processes") {
        //   tspan_name.attr("y", d.height * (-0.5) + 15);
        // }
        // tspan_name.text(d.name);
        var fnobj_name = d3.select(this).select("#fnobj_name");
        fnobj_name.attr("x", d.width * (-0.5));
        fnobj_name.attr("width", d.width);
        fnobj_name.attr("height", d.height - 30);
        if (d.category == "processes") {
          fnobj_name.attr("y", d.height * (-0.5) + 5);
        }
        var tspan_name = d3.select(this).select("#tspan_name");
        tspan_name.html(d.name.replace(/[\n\r]/g,''));
        // 更新节点描述 added by Simon 2018/6/13
        if (!d.desc) {
          d.desc = "";
        }
        var fnobj_desc = d3.select(this).select("#fnobj_desc");
        fnobj_desc.attr("x", d.width * (-0.5));
        fnobj_desc.attr("width", d.width);
        fnobj_desc.attr("height", d.height - 30);
        if (d.category == "processes") {
          fnobj_desc.attr("y", d.height * (-0.5) + 30);
        }
        var tspan_desc = d3.select(this).select("#tspan_desc");
        tspan_desc.html(d.desc.replace(/[\n\r]/g,'<br>'));
      }
      return "translate(" + d.x + "," + d.y + ")";
    });

    // add new nodes
    var newGs = thisGraph.shapes.enter().append("g").attr({
      id: function(d) {
        return d.id;
      }
    }).classed(consts.shapeGClass, true).attr("transform", function(d) {
        return "translate(" + d.x + "," + d.y + ")";
      }).on("mouseover", function(d) {
        if (state.shiftNodeDrag) {
          d3.select(this).classed(consts.connectClass, true);
        }
        if (d3.event.shiftKey) {  // added by Simon 2018/5/28
          d3.select(this).attr("data-cursor", d3.select(this).style("cursor"));
          d3.select(this).style("cursor", "crosshair");
        }
      }).on("mouseout", function(d) {
        d3.select(this).classed(consts.connectClass, false);
        if (d3.select(this).attr("data-cursor")) {  // added by Simon 2018/5/28
          d3.select(this).style("cursor", d3.select(this).attr("data-cursor"));
        }
      }).on("mousedown", function(d) {
        thisGraph.shapeMouseDown.call(thisGraph, d3.select(this), d);
      }).on("mouseup", function(d) {
        thisGraph.shapeMouseUp.call(thisGraph, d3.select(this), d);
      }).call(thisGraph.drag);

    // 绘制各个图形 by Simon 2018/5/13
    // newGs.append("shape").attr("r", String(consts.nodeRadius));
    newGs.each(function(d) {
      switch (d.type) {
        case "start":
          d3.select(this).append("circle").attr("r", String(consts.nodeRadius));
          // draw a triangle // by Simon 5/14/2018
          var points = (consts.nodeRadius * (-0.4))
            + "," + (consts.nodeRadius * (-0.55))
            + " " + (consts.nodeRadius * 0.65)
            + "," + "0"
            + " " + (consts.nodeRadius * (-0.4))
            + "," + (consts.nodeRadius * (0.55));
          d3.select(this).append("polygon").attr("points", points);
          d3.select(this).classed("start", true);
          break;
        case "end":
          d3.select(this).append("circle").attr("r", String(consts.nodeRadius));
          // draw a square // by Simon 5/14/2018
          var points = (consts.nodeRadius * (-0.45))
            + "," + (consts.nodeRadius * (-0.45))
            + " " + (consts.nodeRadius * 0.45)
            + "," + (consts.nodeRadius * (-0.45))
            + " " + (consts.nodeRadius * 0.45)
            + "," + (consts.nodeRadius * 0.45)
            + " " + (consts.nodeRadius * (-0.45))
            + "," + (consts.nodeRadius * 0.45);
          d3.select(this).append("polygon").attr("points", points);
          d3.select(this).classed("end", true);
          break;
        default:  // activity
          thisGraph.drawShape(d3.select(this), d, true);
          break;
      }
      // 绘制文本内容 by Simon 6/13/2018
      var nameDy = -5, descDy = 0;
      if (d.category == "processes") {
        nameDy = d.height * (-0.5) + 5;
        descDy = d.height * (-0.5) + 30;
      }
      // Append name // by Simon 9/2/2018
      // var elName = d3.select(this).append("text").attr("text-anchor", "middle").attr("letter-spacing", "0");
      // elName.append("tspan").text(d.name).attr("id", "tspan_name").attr("x", 0).attr("y", nameDy).attr("style", "font-weight: bold;");
      // elName.style("cursor", "text");
      if (d.type == "activity") {
        var fObjName = d3.select(this).append("foreignObject")
                      .attr("id", "fnobj_name")
                      .attr("style", "overflow:visible;cursor:text;")
                      .attr("pointer-events", "all")
                      .attr("x", d.width * (-0.5))
                      .attr("y", nameDy)
                      .attr("width", d.width)
                      .attr("height", d.height - 30);
        fObjName.append("xhtml:div")
                      .attr("style", "display: inline-block; font-size: 12px; font-family: Helvetica; font-weight: bold; color: rgb(0, 0, 0); line-height: 1.2; vertical-align: top; width: 100%; white-space: normal; word-wrap: normal; text-align: center;")
                      .append("xhtml:div")
                      .attr("xmlns", "http://www.w3.org/1999/xhtml")
                      .attr("id", "tspan_name")
                      .attr("style", "display:inline-block;text-align:inherit;text-decoration:inherit;")
                      .html(d.name.replace(/[\n\r]/g,''));
        if (d.activityType != "TABLE") {
          fObjName.on("dblclick", function(d) {
            // prevent d3 events
            d3.event.stopPropagation(); 
            d3.event.preventDefault();
            // get offsets
            var translate = thisGraph.zoom.translate();
            var scale = thisGraph.zoom.scale();
            if (!translate[0]) translate = [0, 0];
            if (!scale) scale = 1;
            var left = (d.x + translate[0]) * scale;
            var top = (d.y + translate[1] - 5) * scale;
            var maxWidth = d.width;
            if (d.category == "processes") {
              top = top - (d.height * 0.5 - 15) * scale;
            }
            var fontSize = 12 * scale;
            // disable key down overrides
            thisGraph.state.graphKeyDown = false;
            // hide elName
            this.style.visibility = "hidden";
            // create div
            var nameEditorDiv = document.createElement("div");
            nameEditorDiv.setAttribute("contenteditable", "true");
            nameEditorDiv.setAttribute("id", "nameEditor");
            nameEditorDiv.setAttribute("style", "min-height: 1em; position: relative; line-height: 1.2; font-weight: bold; font-size: " + fontSize + "px; font-family: Helvetica; text-align: center; outline: none; color: rgb(0, 0, 0); word-wrap: normal; white-space: normal; max-width: " + maxWidth + "px; left: " + left + "px; top: " + top + "px; transform-origin: 0px 0px 0px; transform: scale(1, 1) translate(-50%, 0);");
            nameEditorDiv.innerHTML = d.name.replace(/[\n\r]/g,'');
            // append div
            var svgBgDiv = document.getElementById("svgbg");
            svgBgDiv.appendChild(nameEditorDiv);
            // set div selected
            if (document.body.createTextRange) {
              var range = document.body.createTextRange();
              range.moveToElementText(nameEditorDiv);
              range.select();
            } else if (window.getSelection) {
                var selection = window.getSelection();
                var range = document.createRange();
                range.selectNodeContents(nameEditorDiv);
                selection.removeAllRanges();
                selection.addRange(range);        
            }
            // prevent document click
            $("#nameEditor").on("click", function(ev) {
              ev.stopPropagation();
              ev.preventDefault();
            })
          });
        }
      }
      // Append description // by Simon 7/8/2018
      // el.append("tspan").text(d.desc).attr("id", "tspan_desc").attr("x", 0).attr("y", descDy);
      if (d.type == "activity" && d.category == "processes") {
        if (!d.desc) {
          d.desc = "";
        }
        var fObjDesc = d3.select(this).append("foreignObject")
                    .attr("id", "fnobj_desc")
                    .attr("style", "overflow:visible;cursor:text;")
                    .attr("pointer-events", "all")
                    .attr("x", d.width * (-0.5))
                    .attr("y", descDy)
                    .attr("width", d.width)
                    .attr("height", d.height - 30);
        fObjDesc.append("xhtml:div")
                    .attr("style", "display: inline-block; font-size: 12px; font-family: Helvetica; color: rgb(0, 0, 0); line-height: 1.2; vertical-align: top; width: 100%; white-space: normal; word-wrap: normal; text-align: center;")
                    .append("xhtml:div")
                    .attr("xmlns", "http://www.w3.org/1999/xhtml")
                    .attr("id", "tspan_desc")
                    .attr("style", "display:inline-block;text-align:inherit;text-decoration:inherit;")
                    .html(d.desc.replace(/[\n\r]/g,'<br>'));
        fObjDesc.on("dblclick", function(d) {
          // prevent d3 events
          d3.event.stopPropagation(); 
          d3.event.preventDefault();
          // get offsets
          var translate = thisGraph.zoom.translate();
          var scale = thisGraph.zoom.scale();
          if (!translate[0]) translate = [0, 0];
          if (!scale) scale = 1;
          var left = (d.x + translate[0]) * scale;
          var top = (d.y + translate[1]) * scale;
          var maxWidth = d.width;
          if (d.category == "processes") {
            top = top - (d.height * 0.5 - 30) * scale;
          }
          var fontSize = 12 * scale;
          // disable key down overrides
          thisGraph.state.graphKeyDown = false;
          // hide elName
          this.style.visibility = "hidden";
          // create div
          var descEditorDiv = document.createElement("div");
          descEditorDiv.setAttribute("contenteditable", "true");
          descEditorDiv.setAttribute("id", "descEditor");
          descEditorDiv.setAttribute("style", "min-height: 1em; position: relative; line-height: 1.2; font-weight: normal; font-size: " + fontSize + "px; font-family: Helvetica; text-align: center; outline: none; color: rgb(0, 0, 0); word-wrap: normal; white-space: normal; max-width: " + maxWidth + "px; left: " + left + "px; top: " + top + "px; transform-origin: 0px 0px 0px; transform: scale(1, 1) translate(-50%, 0);");
          descEditorDiv.innerHTML = d.desc.replace(/[\n\r]/g,'<br>');
          // append div
          var svgBgDiv = document.getElementById("svgbg");
          svgBgDiv.appendChild(descEditorDiv);
          // set div selected
          if (document.body.createTextRange) {
            var range = document.body.createTextRange();
            range.moveToElementText(descEditorDiv);
            range.select();
          } else if (window.getSelection) {
              var selection = window.getSelection();
              var range = document.createRange();
              range.selectNodeContents(descEditorDiv);
              selection.removeAllRanges();
              selection.addRange(range);        
          }
          // prevent document click
          $("#descEditor").on("click", function(ev) {
            ev.stopPropagation();
            ev.preventDefault();
          })
        });
      }
    });

    // remove old nodes
    thisGraph.shapes.exit().remove();    

    // process pathes
    thisGraph.paths = thisGraph.paths.data(edges, function(d) {
      return String(d.source.id) + "+" + String(d.target.id);
    });
    var paths = thisGraph.paths;
    
    // update existing paths
    var link = paths.selectAll("path").attr("d", function(d) {        
        if (d.drawLine == "NOROUTING") {
          var start = {
            id: d.source.id,
            x: d.source.x,
            y: d.source.y,
            width: d.source.width || d.source.radius,
            height: d.source.height || d.source.radius
          };
          var des = {
            id: d.target.id,
            x: d.target.x,
            y: d.target.y,
            width: d.target.width || d.target.radius,
            height: d.target.height || d.target.radius
          };
          return thisGraph.getLinkNoRouting(start, des);
        }
        if (d.drawLine == "SIMPLEROUTING") {
          var start = {
            id: d.source.id,
            x: d.source.x,
            y: d.source.y,
            width: d.source.width || d.source.radius,
            height: d.source.height || d.source.radius
          };
          var des = {
            id: d.target.id,
            x: d.target.x,
            y: d.target.y,
            width: d.target.width || d.target.radius,
            height: d.target.height || d.target.radius
          };
          return thisGraph.getLinkSimpleRouting(start, des);
        }
      }).style("marker-end", function(d) {
        return thisGraph.addMarkerEnd(d);   // by Simon 2018/5/13
      });
    refresh(link); // 兼容IE11

    // add new paths
    var newPs = thisGraph.paths.enter().append("g").attr({
      id: function(d) {
        return d.id;
      }
    }).on("mousedown", function(d) {
      thisGraph.pathMouseDown.call(thisGraph, d3.select(this), d);
    }).on("mouseup", function(d) {
      state.mouseDownLink = null;
    });
    
    // 绘制连线 by Simon 7/7/2018
    newPs.each(function(d) {
      // 连接线背景
      d3.select(this).append("path").classed("linkBG", true).attr("d", function(d) {
        if (d.drawLine == "NOROUTING") {
          var start = {
            id: d.source.id,
            x: d.source.x,
            y: d.source.y,
            width: d.source.width || d.source.radius,
            height: d.source.height || d.source.radius
          };
          var des = {
            id: d.target.id,
            x: d.target.x,
            y: d.target.y,
            width: d.target.width || d.target.radius,
            height: d.target.height || d.target.radius
          };
          return thisGraph.getLinkNoRouting(start, des);
        }
        if (d.drawLine == "SIMPLEROUTING") {
          var start = {
            id: d.source.id,
            x: d.source.x,
            y: d.source.y,
            width: d.source.width || d.source.radius,
            height: d.source.height || d.source.radius
          };
          var des = {
            id: d.target.id,
            x: d.target.x,
            y: d.target.y,
            width: d.target.width || d.target.radius,
            height: d.target.height || d.target.radius
          };
          return thisGraph.getLinkSimpleRouting(start, des);
        }
      });
      // 连接线
      d3.select(this).append("path").classed("link", true).attr("d", function(d) {
        if (d.drawLine == "NOROUTING") {
          var start = {
            id: d.source.id,
            x: d.source.x,
            y: d.source.y,
            width: d.source.width || d.source.radius,
            height: d.source.height || d.source.radius
          };
          var des = {
            id: d.target.id,
            x: d.target.x,
            y: d.target.y,
            width: d.target.width || d.target.radius,
            height: d.target.height || d.target.radius
          };
          return thisGraph.getLinkNoRouting(start, des);
        }
        if (d.drawLine == "SIMPLEROUTING") {
          var start = {
            id: d.source.id,
            x: d.source.x,
            y: d.source.y,
            width: d.source.width || d.source.radius,
            height: d.source.height || d.source.radius
          };
          var des = {
            id: d.target.id,
            x: d.target.x,
            y: d.target.y,
            width: d.target.width || d.target.radius,
            height: d.target.height || d.target.radius
          };
          return thisGraph.getLinkSimpleRouting(start, des);
        }
      }).attr("linkType", function(d) {
        return d.linkType;                  // by Simon 2018/6/15
      }).style("marker-end", function(d) {
        return thisGraph.addMarkerEnd(d);   // by Simon 2018/5/13
      });
    });

    // remove old links
    paths.exit().remove();

    // process selection by Simon 6/8/2018
    var selection = [];    
    if (state.selectedNode && state.selectedNode.type == "activity") {
      // get outline width and height
      var width = state.selectedNode.outlineWidth, height = state.selectedNode.outlineHeight;
      // init selection
      selection = [
        {
          id: generateUUID("slct"), type: "rect", x: state.selectedNode.x, y: state.selectedNode.y, width: width, height: height
        },
        {
          id: generateUUID("slct"), type: "dot", x: state.selectedNode.x, y: state.selectedNode.y, dx: width * (-0.5) - 9, dy: height * (-0.5) - 9, cursor: "nw-resize"
        },
        {
          id: generateUUID("slct"), type: "dot", x: state.selectedNode.x, y: state.selectedNode.y, dx: -9, dy: height * (-0.5) - 9, cursor: "n-resize"
        },
        {
          id: generateUUID("slct"), type: "dot", x: state.selectedNode.x, y: state.selectedNode.y, dx: width * (0.5) - 9, dy: height * (-0.5) - 9, cursor: "ne-resize"
        },
        {
          id: generateUUID("slct"), type: "dot", x: state.selectedNode.x, y: state.selectedNode.y, dx: width * (-0.5) - 9, dy: -9, cursor: "w-resize"
        },
        {
          id: generateUUID("slct"), type: "dot", x: state.selectedNode.x, y: state.selectedNode.y, dx: width * (0.5) - 9, dy: -9, cursor: "e-resize"
        },
        {
          id: generateUUID("slct"), type: "dot", x: state.selectedNode.x, y: state.selectedNode.y, dx: width * (-0.5) - 9, dy: height * (0.5) - 9, cursor: "sw-resize"
        },
        {
          id: generateUUID("slct"), type: "dot", x: state.selectedNode.x, y: state.selectedNode.y, dx: -9, dy: height * (0.5) - 9, cursor: "s-resize"
        },
        {
          id: generateUUID("slct"), type: "dot", x: state.selectedNode.x, y: state.selectedNode.y, dx: width * (0.5) - 9, dy: height * (0.5) - 9, cursor: "se-resize"
        }
      ];    
    }

    thisGraph.selection = thisGraph.selection.data(selection, function(d) {
      return d.id;
    });

    thisGraph.selection.attr("transform", function(d) {
      return "translate(" + d.x + "," + d.y + ")";
    });

    // add new selection by Simon 6/8/2018
    var newSel = thisGraph.selection.enter().append("g").attr({
      id: function(d) {
        return d.id;
      }
    });
    newSel.attr("transform", function(d) {
      return "translate(" + d.x + "," + d.y + ")";
    }).on("mousedown", function(d) {
      thisGraph.selDotMouseDown.call(thisGraph, d3.select(this), d);
    }).on("mouseup", function(d) {
      thisGraph.selDotMouseUp.call(thisGraph, d3.select(this), d);
    }).call(thisGraph.dragSelDot);

    // draw selection
    newSel.each(function(d) {
      switch (d.type) {
        case "rect":
          d3.select(this).append("rect")
            .attr("x", d.width * (-0.5))
            .attr("y", d.height * (-0.5))
            .attr("width", d.width)
            .attr("height", d.height)
            .attr("fill", "none")
            .attr("stroke", "#00a8ff")
            .attr("stroke-dasharray", "3 3")
            .attr("pointer-event", "none");
          break;
        case "dot":
          d3.select(this).append("image")
            .attr("x", d.dx)
            .attr("y", d.dy)
            .attr("width", "18")
            .attr("height", "18")
            .attr("xlink:href", "data:image/svg+xml;base64,PCFET0NUWVBFIHN2ZyBQVUJMSUMgIi0vL1czQy8vRFREIFNWRyAxLjEvL0VOIiAiaHR0cDovL3d3dy53My5vcmcvR3JhcGhpY3MvU1ZHLzEuMS9EVEQvc3ZnMTEuZHRkIj48c3ZnIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyIgeG1sbnM6eGxpbms9Imh0dHA6Ly93d3cudzMub3JnLzE5OTkveGxpbmsiIHdpZHRoPSIxOHB4IiBoZWlnaHQ9IjE4cHgiIHZlcnNpb249IjEuMSI+PGNpcmNsZSBjeD0iOSIgY3k9IjkiIHI9IjUiIHN0cm9rZT0iI2ZmZiIgZmlsbD0iIzAwN2RmYyIgc3Ryb2tlLXdpZHRoPSIxIi8+PC9zdmc+")
            .attr("preserveAspectRatio", "none")
            .attr("style", "cursor: " + d.cursor + "; visibility: visible;")
          break;
      }
    });

    // remove old selection
    // Fixed https://stackoverflow.com/questions/42076672/removing-svg-element-in-d3-that-is-draggable-results-in-cannot-read-property-ow
    thisGraph.selection.exit().on('mousedown.drag', null);
    thisGraph.selection.exit().remove();
  };

  // /**
  //   * 根据缩放比例和偏移量转换坐标
  //   * @param  {DOM}    svgContainer .svgContainer元素
  //   * @param  {Object} position     位置坐标
  //   * @return {Object} position     转换后的坐标
  //   */
  GraphEditor.prototype.parsePosition = function(svgContainer, position) {
    var transform = $(svgContainer)
      .find(".graph")
      .attr("transform"); // transform="translate(20,11) scale(1)"
    if (transform) {
      var result = []; // ['20,11', '1']
      var regExp_ = /\(([^)]*)\)/g;
      var ele;
      while ((ele = regExp_.exec(transform)) != null) {
        result.push(ele[1]);
      }
      var translate = (result[0] && result[0].split(/,|\s/)) || [0, 0]; // IE11 result[0] 为 '23.45 22.87'
      var scale = (result[1] && result[1].split(",")[0]) || 1;
      if (translate.length == 1 && translate[0] == 0) {
        // 兼容IE11
        translate.push(0);
      }
      position.x = (position.x - translate[0]) / scale;
      position.y = (position.y - translate[1]) / scale;
    }
    return position;
  };

  // 缩放
  GraphEditor.prototype.zoomed = function() {
    this.state.justScaleTransGraph = true;
    var translate = this.zoom.translate();
    var scale = this.zoom.scale();
    if (!translate[0]) translate = [0, 0];
    if (!scale) scale = 1;
    // fixed by Simon 2018/5/12
    // console.log(".full-right>.tab.active ." + this.consts.graphClass);
    // d3.select(".full-right>.tab.active ." + this.consts.graphClass)
    //  .attr("transform", "translate(" + translate + ") scale(" + scale + ")");
    d3.select("." + this.consts.graphClass)
      .attr("transform", "translate(" + translate + ") scale(" + scale + ")");
  };

export default GraphEditor;