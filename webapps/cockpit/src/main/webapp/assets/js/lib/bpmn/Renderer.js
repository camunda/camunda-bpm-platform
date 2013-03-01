/**
 * The BPMN 2.0 SVG renderer module
 *
 * This module provides the functionality for rendering a BPMN 2.0 Process Model in SVG
 */
define(["dojox/gfx", "dojo/_base/lang", "dojo/dom-construct", "dojo/_base/window", "dojo/query", "dojo/dom", "dojo/dom-class"], function (gfx, lang, domConstruct, win, query, dom, domClass) {
  var eventDefinitionPaths = {
    "messagecatch":" M7 10  L7 20  L23 20  L23 10  z M7 10  L15 16  L23 10 ",
    "messagethrow":"M7 9  L15 15  L23 9  z M7 10  L7 20  L23 20  L23 10  L15 16  z",
    "timerEventDefinition":" M15 5  L15 8  M20 6  L18.5 9  M24 10  L21 11.5  M25 15  L22 15  M24 20  L21 18.5  M20 24  L18.5 21  M15 25  L15 22  M10 24  L11.5 21  M6 20  L9 18.5  M5 15  L8 15  M6 10  L9 11.5  M10 6  L11.5 9  M17 8  L15 15  L19 15 ",
    "errorEventDefinition": " M21.820839 10.171502  L18.36734 23.58992  L12.541380000000002 13.281818999999999  L8.338651200000001 19.071607  L12.048949000000002 5.832305699999999  L17.996148000000005 15.132659  L21.820839 10.171502  z",
    "escalation": "M15 7.75  L21 22.75  L15 16  L9 22.75  z",
    "signalEventDefinition": "M7.7124971 20.247342  L22.333334 20.247342  L15.022915000000001 7.575951200000001  L7.7124971 20.247342  z",
    "cancel": " M6.283910500000001 9.27369  L9.151395 6.4062062  L14.886362000000002 12.141174  L20.621331 6.4062056  L23.488814 9.273689  L17.753846 15.008657  L23.488815 20.743626  L20.621331 23.611111  L14.886362000000002 17.876142  L9.151394 23.611109  L6.283911000000001 20.743625  L12.018878 15.008658  L6.283910500000001 9.27369  z",
    "conditional": " M6 6  L24 6 L24 24 L6 24 L6 6 M9 9  L21 9  M9 13  L21 13  M9 17  L21 17  M9 21  L21 21 z",
    "compensate": "M14 8 L14 22 L7 15 L14 8 M21 8 L21 22 L14 15 L21 8 z",
    "multipleParallel": "M5.75 12  L5.75 18  L12 18  L12 24.75  L18 24.75  L18 18  L24.75 18  L24.75 12  L18 12  L18 5.75  L12 5.75  L12 12  z",
    "multiple": " M19.834856 21.874369  L9.762008 21.873529  L6.650126 12.293421000000002  L14.799725 6.373429600000001  L22.948336 12.294781  L19.834856 21.874369  z",
    "link": "M9 13 L18 13 L18 10 L23 15 L18 20 L18 17 L8 17 L8 13"
  }

  var taskDefinitionPaths = {
    // Public Domain: http://thenounproject.com/noun/user/#icon-No1331
    "userTask": "M60.541,28.82c0.532,2.353,1.176,4.893,1.301,7.342c0.033,0.654,0.072,1.512-0.201,2.07  c2.227,1.482,1.137,4.562-0.166,6.129c-0.469,0.562-1.535,1.26-1.773,1.957c-0.352,1.025-0.787,2.031-1.408,2.938  c-0.519,0.756-0.408,0.184-0.925,1.344c-0.35,1.576-0.881,5.145-0.13,6.61c0.986,1.921,3.146,3.137,4.934,4.159  c2.37,1.356,5.018,2.351,7.549,3.362c2.33,0.931,4.76,1.626,7.002,2.764c0.703,0.356,1.412,0.704,2.078,1.128  c0.537,0.342,1.438,0.869,1.566,1.559v5.424h-60.01l0.041-5.424c0.128-0.689,1.029-1.217,1.566-1.559  c0.666-0.424,1.375-0.771,2.078-1.128c2.242-1.138,4.673-1.833,7.002-2.764c2.531-1.012,5.178-2.006,7.549-3.362  c1.787-1.022,3.947-2.238,4.933-4.159c0.752-1.466,0.332-5.05-0.019-6.624l0,0c-0.601-0.389-1.016-1.594-1.357-2.197  c-0.359-0.637-0.648-1.324-1.086-1.914c-0.597-0.805-1.592-1.182-2.242-1.936c-0.434-0.502-0.619-1.124-0.834-1.74  c-0.257-0.736-0.131-1.334-0.246-2.161c-0.051-0.354,0.13-0.765,0.34-1.064c0.258-0.368,0.728-0.44,0.847-0.906  c0.147-0.577-0.177-1.253-0.239-1.823c-0.066-0.609-0.224-1.58-0.221-2.191c0.01-2.217-0.4-4.217,1.375-5.969  c0.624-0.614,1.333-1.145,2.01-1.699l0,0c0.26-0.828,1.507-1.338,2.236-1.616c0.947-0.36,1.943-0.562,2.914-0.851  c2.93-0.873,6.297-0.78,8.866,1.029c0.843,0.594,2.005,0.084,2.893,0.594C59.619,26.634,60.639,27.771,60.541,28.82z",
    // Public Domain: http://thenounproject.com/noun/gear/#icon-No1329
    "serviceTask": "M95.784,59.057c1.867,0,3.604-1.514,3.858-3.364c0,0,0.357-2.6,0.357-5.692c0-3.092-0.357-5.692-0.357-5.692  c-0.255-1.851-1.991-3.364-3.858-3.364h-9.648c-1.868,0-3.808-1.191-4.31-2.646s-1.193-6.123,0.128-7.443l6.82-6.82  c1.32-1.321,1.422-3.575,0.226-5.01L80.976,11c-1.435-1.197-3.688-1.095-5.01,0.226l-6.82,6.82c-1.32,1.321-3.521,1.853-4.888,1.183  c-1.368-0.67-5.201-3.496-5.201-5.364V4.217c0-1.868-1.514-3.604-3.364-3.859c0,0-2.6-0.358-5.692-0.358s-5.692,0.358-5.692,0.358  c-1.851,0.254-3.365,1.991-3.365,3.859v9.648c0,1.868-1.19,3.807-2.646,4.31c-1.456,0.502-6.123,1.193-7.444-0.128l-6.82-6.82  C22.713,9.906,20.459,9.804,19.025,11L11,19.025c-1.197,1.435-1.095,3.689,0.226,5.01l6.819,6.82  c1.321,1.321,1.854,3.521,1.183,4.888s-3.496,5.201-5.364,5.201H4.217c-1.868,0-3.604,1.514-3.859,3.364c0,0-0.358,2.6-0.358,5.692  c0,3.093,0.358,5.692,0.358,5.692c0.254,1.851,1.991,3.364,3.859,3.364h9.648c1.868,0,3.807,1.19,4.309,2.646  c0.502,1.455,1.193,6.122-0.128,7.443l-6.819,6.819c-1.321,1.321-1.423,3.575-0.226,5.01L19.025,89  c1.435,1.196,3.688,1.095,5.009-0.226l6.82-6.82c1.321-1.32,3.521-1.853,4.889-1.183c1.368,0.67,5.201,3.496,5.201,5.364v9.648  c0,1.867,1.514,3.604,3.365,3.858c0,0,2.599,0.357,5.692,0.357s5.692-0.357,5.692-0.357c1.851-0.255,3.364-1.991,3.364-3.858v-9.648  c0-1.868,1.19-3.808,2.646-4.31s6.123-1.192,7.444,0.128l6.819,6.82c1.321,1.32,3.575,1.422,5.01,0.226L89,80.976  c1.196-1.435,1.095-3.688-0.227-5.01l-6.819-6.819c-1.321-1.321-1.854-3.521-1.183-4.889c0.67-1.368,3.496-5.201,5.364-5.201H95.784  z M50,68.302c-10.108,0-18.302-8.193-18.302-18.302c0-10.107,8.194-18.302,18.302-18.302c10.108,0,18.302,8.194,18.302,18.302  C68.302,60.108,60.108,68.302,50,68.302z",
    "scriptTask": "M6.402,0.5h14.5c0,0-5.833,2.833-5.833,5.583s4.417,6,4.417,9.167    s-4.167,5.083-4.167,5.083H0.235c0,0,5-2.667,5-5s-4.583-6.75-4.583-9.25S6.402,0.5,6.402,0.5z"
  }

  var activityMarkerPaths = {
    "loop": "M 0 0 L 0 3 L -3 3 M 0 3 A 4.875,4.875 0 1 1 4 3",
    "miSeq": "M 0 -2 h10 M 0 2 h10 M 0 6 h10",
    "miPar": "M 0 -2 v8 M 4 -2 v8 M 8 -2 v8",
    "adhoc": "m 0 0 c -0.54305,0.60192 -1.04853,1.0324 -1.51647,1.29142 -0.46216,0.25908 -0.94744,0.38857 -1.4558,0.38857 -0.57194,0 -1.23628,-0.22473 -1.99307,-0.67428 -0.0577,-0.0306 -0.10111,-0.0534 -0.12999,-0.0687 -0.0346,-0.0228 -0.0896,-0.0533 -0.16464,-0.0915 -0.80878,-0.47234 -1.4558,-0.70857 -1.94107,-0.70857 -0.46217,0 -0.91566,0.14858 -1.36047,0.44576 -0.44485,0.2895 -0.92434,0.75046 -1.43849,1.38285 l 0,-2.03429 c 0.54881,-0.60194 1.05431,-1.0324 1.51647,-1.29147 0.46793,-0.26666 0.9532,-0.39999 1.45581,-0.39999 0.57191,0 1.24205,0.22856 2.01039,0.68574 0.0461,0.0308 0.0838,0.0533 0.11266,0.0687 0.0404,0.0228 0.0982,0.0533 0.1733,0.0913 0.803,0.4724 1.45002,0.70861 1.94108,0.70857 0.44481,4e-5 0.88676,-0.14475 1.32581,-0.43429 0.43905,-0.2895 0.9272,-0.75425 1.46448,-1.39428",
    "compensate": "M 50 70 L 55 65 L 55 75z M44.7 70 L49.7 75 L 49.7 65z"
  }

  var regularStroke = "#888";
  var highlightStroke = "darkOrange";

  var generalStyle = {
    stroke: regularStroke,
    "stroke-width": 2,
    "stroke-linecap": "round",
    "stroke-linejoin": "round",
    "stroke-opacity" : 1
  }

  var groupStyle = {
    stroke: regularStroke,
    "stroke-width": 2,
    "stroke-opacity" : 1
  }

  var dataObjectStyle = {
    stroke: regularStroke,
    "stroke-width": 2,
    "stroke-opacity" : 1
  }

  var eventStyle = {
     stroke: regularStroke,
    "stroke-width": 1.5,
    "fill": "white"
  };

  var endEventStyle = {
    "stroke-width": 3
  };

  var activityStyle = {
    stroke: regularStroke,
    "stroke-width": 2,
    "stroke-linecap": "round",
    "stroke-linejoin": "round",
    "stroke-opacity" : 1,
    "fill": "white"
  };

  var gatewayStyle = {
    "fill": "white"
  };

  var participantStyle = {
    "stroke": regularStroke
  };

  var laneStyle = {
    "stroke": "#ccc"
  };

  var gatewayMarkerStyle = {
    stroke: regularStroke,
    fill :  regularStroke,
    "stroke-opacity" : 1,
    "stroke-width": 4
  };

  var sequenceFlowStyle = {
    "fill" : regularStroke,
    "stroke-width": 2,
    "arrow-end": "block-midium-midium",
    "stroke-linecap": "square",
    "stroke-linejoin": "round"
  };

  var messageFlowStyle = {
    "stroke-width": 2,
    "arrow-end": "open-wide-long",
    "stroke-dasharray": "-",
    "stroke-linecap": "round",
    "stroke-linejoin": "round"
  };

  var textStyle = {
    "font-size": 12,
    "font-family": "Arial, Helvetica, sans-serif"
  };

  var textBigStyle = {
    "font-size": 20,
    "font-family": "Arial, Helvetica, sans-serif"
  };

  var styleMap = {
    "startEvent" : eventStyle,
    "endEvent" : lang.mixin(lang.clone(eventStyle), endEventStyle),
    "exclusiveGateway" : generalStyle,
    "inclusiveGateway" : generalStyle,
    "userTask" : activityStyle,
    "serviceTask" : activityStyle,
    "manualTask" : activityStyle,
    "process" : participantStyle,
    "lane" : laneStyle,
    "sequenceFlow" : lang.mixin(lang.clone(generalStyle), sequenceFlowStyle)
  };

  function renderLabel(elementRenderer, group, bounds) {
    var baseElement = elementRenderer.baseElement;

    if (!baseElement.name) {
      return;
    }

    var name = baseElement.name.replace("&#xD;", "\n");

    var words = name.split(" ");
    var maxWidth = 100;

    var tempText = "";
    var font = { family: "Arial", size: "9pt", weight: "normal" };

    var labelBounds = elementRenderer.getLabelBounds();
    var pos = labelBounds ? {x: +labelBounds.x, y: labelBounds.y} : {x: +bounds.x, y: +bounds.y};

    for (var i=0; i<words.length; i++) {
      var text = group.createText({x:pos.x, y: pos.y, text: tempText + " " + words[i], align: "middle" })
        .setFont(font) //set font
        .setFill("black");

      if (text.getTextWidth() > maxWidth) {
        tempText += '\n' + words[i];
      } else {
        tempText += " " + words[i];
      }
      text.getParent().remove(text);
    }

    var textLines = tempText.substring(1).split("\n");
    for (var i=0; i<textLines.length; i++) {
      var text = group.createText({ x: pos.x, y: pos.y + i * 10, text: textLines[i], align: "middle" })
        .setFont(font) //set font
        .setFill("black");
    }
    return group;
  }

  var processRenderer = {
    render : function(elementRenderer, gfxGroup) {
      var baseElement = elementRenderer.baseElement;
      var style = elementRenderer.getStyle();
      var bounds = elementRenderer.getBounds();

      // no participant bounds
      if (!bounds) {
        return;
      }

      var x = +bounds.x;
      var y = +bounds.y;
      var width = +bounds.width;
      var height = +bounds.height;

      var processGroup = gfxGroup.createGroup();
      processGroup.setTransform({dx :x, dy:y});

      var rect = processGroup.createRect({ x: 0, y: 0, width: width, height: height});
      rect.setStroke(style.stroke);

      var text = processGroup.createText({ x: 0, y: 0, text: elementRenderer.baseElement.name});

      text.setFont({ family: "Arial", size: "9pt", weight: "normal", align: "middle"}) //set font
      text.setFill("black");

      text.setTransform([gfx.matrix.translate(15, height/2 +30), gfx.matrix.rotateg(-90) ]);

      var separator = processGroup.createLine({ x1: 30, y1: 0, x2: 30, y2: height});
      separator.setStroke(style.stroke);
    }
  };

  var laneRenderer = {
    render : function(elementRenderer, gfxGroup) {
      var baseElement = elementRenderer.baseElement;
      var style = elementRenderer.getStyle();
      var bounds = elementRenderer.getBounds();

      var x = +bounds.x;
      var y = +bounds.y;
      var width = +bounds.width;
      var height = +bounds.height;

      var laneGroup = gfxGroup.createGroup();
      laneGroup.setTransform({dx :x, dy:y});

      var rect = laneGroup.createRect({ x: 0, y: 0, width: width, height: height});
      rect.setStroke(style.stroke);

      var text = laneGroup.createText({ x: 0, y: 0, text: elementRenderer.baseElement.name});

      text.setFont({ family: "Arial", size: "9pt", weight: "normal", align: "middle"}) //set font
      text.setFill("black");

      text.setTransform([gfx.matrix.translate(10, height/2 + 30), gfx.matrix.rotateg(-90) ]);
    }
  };

  var sequenceFlowRenderer = {
    render : function(elementRenderer, gfxGroup) {
      var baseElement = elementRenderer.baseElement;
      var style = elementRenderer.getStyle();
      var waypoints = elementRenderer.getWaypoints();

      var flowGroup = gfxGroup.createGroup();

      var line = flowGroup.createPolyline(waypoints);
      line.setStroke(style.stroke);

      var secondLastPoint = waypoints[waypoints.length-2];
      var lastPoint = waypoints[waypoints.length-1];

      var vector = {x:lastPoint.x - secondLastPoint.x, y: lastPoint.y - secondLastPoint.y};

      var xsize = 6;
      var ysize = 4;

      var svgPath = "M" + lastPoint.x +
        " " + lastPoint.y +
        " L"+ (lastPoint.x - xsize) +
        " " + (lastPoint.y + ysize) +
        " L"+ (lastPoint.x - xsize) +
        " " + (lastPoint.y - ysize) +
        " Z";

      var arrowGroup = flowGroup.createGroup();
      var arrowPath = arrowGroup.createPath(svgPath);
      var theta = Math.atan2(-vector.y, vector.x);

      arrowPath.setStroke(style.stroke);
      arrowPath.setFill(style.fill);

      var sumx = 0;
      var sumy = 0;
      var count = 0;

      for (var index in waypoints) {
        var waypoint = waypoints[index];
        var factor = 1;

        if (index == 0 && waypoints.length > 2) {
          factor = 20;
        }

        sumx += +waypoint.x * factor;
        sumy += +waypoint.y * factor;
        count+= factor;
      }

      arrowPath.setTransform([gfx.matrix.rotateAt(-theta, lastPoint)]);
      renderLabel(elementRenderer, gfxGroup, {x: sumx / count, y: sumy / count});
    }
  };

  var gatewayRenderer = {
    render : function(elementRenderer, gfxGroup) {
      var baseElement = elementRenderer.baseElement;
      var style = elementRenderer.getStyle();
      var bounds = elementRenderer.getBounds();

      var x = +bounds.x;
      var y = +bounds.y;
      var width = +bounds.width;
      var height = +bounds.height;

      var gatewayGroup = gfxGroup.createGroup();
      gatewayGroup.setTransform({dx :x, dy:y});

      var rect = gatewayGroup.createPolyline([
        {x: width/2, y: 0},
        {x: width, y: height/2},
        {x: width/2, y: height},
        {x: 0, y: height/2},
        {x: width/2, y: 0}
      ]);

      rect.setStroke(style.stroke);
      rect.setFill(style.fill);

      var symbolGroup = gatewayGroup.createGroup();
      var symbolSize = (height/2) * 0.80;
      var stroke = {color: style.stroke, width : style["stroke-width"]};

      switch (baseElement.type) {
        case "exclusiveGateway":

          var symbol = symbolGroup.createText({ x: width/2, y: height /2, text: "X", align: "middle" })
            .setFont({ family: "Arial", size: symbolSize+"pt"}) //set font
            .setStroke(stroke)
            .setFill(gatewayMarkerStyle.fill);
          symbol.setTransform({dy: symbolSize/2, dx: 0});
          break;
        case "inclusiveGateway":
          var symbol = symbolGroup.createCircle({ cx: width/2, cy: height/2, r: symbolSize*0.5 })
            .setStroke(stroke);
          break;
      }

      gatewayGroup.createText({ x: width/2, y: height + 10, text: elementRenderer.baseElement.name, align: "middle" })
        .setFont({ family: "Arial", size: "9pt", weight: "normal" }) //set font
        .setFill("black");
    }
  };

  var taskRenderer = {
    render : function(elementRenderer, gfxGroup) {
      var baseElement = elementRenderer.baseElement;
      var style = elementRenderer.getStyle();
      var bounds = elementRenderer.getBounds();

      var x = +bounds.x;
      var y = +bounds.y;
      var width = +bounds.width;
      var height = +bounds.height;

      var taskGroup = gfxGroup.createGroup();
      taskGroup.setTransform({dx :x, dy:y});

      var rect = taskGroup.createRect({ x: 0, y: 0, width: width, height: height, r: 5 });
      rect.setStroke(style.stroke);
      rect.setFill(style.fill);

      if (taskDefinitionPaths[baseElement.type]) {
        var path = taskGroup.createPath(taskDefinitionPaths[baseElement.type]);
        path.setStroke({color : style.stroke, width: 3});
        path.setTransform({xx: 0.25, yy: 0.25});
      }

      var text = renderLabel(elementRenderer, gfxGroup, {x: x + width /2 , y: y + height /2});
    }
  };

  var eventRenderer = {
    render : function(elementRenderer, gfxGroup) {
      var style = elementRenderer.getStyle();
      var bounds = elementRenderer.getBounds();

      var x = +bounds.x;
      var y = +bounds.y;
      var rad = +bounds.width / 2;

      // render basic circle
      var circleGroup = gfxGroup.createGroup();
      circleGroup.setTransform({dx :x, dy:y});

      var circle = circleGroup.createCircle({cx :rad, cy :rad, r:rad});

      var element = elementRenderer.baseElement;
      var eventType = elementRenderer.getEventType();

      var strokeStyle = "Solid";
      // mark as non-interrupting if necessary
      if (element.cancelActivity == false) {
        strokeStyle = "Dash";
      }

      circle.setStroke({color: style.stroke, style: strokeStyle, width: style["stroke-width"]});

      if (element.eventDefinitions && element.eventDefinitions.length > 0) {
        // FIXME only looking for the first one for now
        var definitionType = element.eventDefinitions[0].type;
        var typeLookup = definitionType;

        if (/^message/i.test(definitionType)) {
          typeLookup = "message"+eventType;
        }

        var path = circleGroup.createPath(eventDefinitionPaths[typeLookup]);
        path.setStroke(style.stroke);
      }

      renderLabel(elementRenderer, gfxGroup, {x : x + +bounds.width / 2, y : y + +bounds.width + rad});

      return circle;
    }
  };

  // build up the map of renderers
  var RENDERER_DELEGATES = {};
  RENDERER_DELEGATES["process"] = processRenderer;
  RENDERER_DELEGATES["startEvent"] = eventRenderer;
  RENDERER_DELEGATES["endEvent"] = eventRenderer;
  RENDERER_DELEGATES["userTask"] = taskRenderer;
  RENDERER_DELEGATES["serviceTask"] = taskRenderer;
  RENDERER_DELEGATES["manualTask"] = taskRenderer;
  RENDERER_DELEGATES["exclusiveGateway"] = gatewayRenderer;
  RENDERER_DELEGATES["inclusiveGateway"] = gatewayRenderer;
  RENDERER_DELEGATES["sequenceFlow"] = sequenceFlowRenderer;
  RENDERER_DELEGATES["lane"] = laneRenderer;

  var RenderingException = (function () {

    function RenderingException(message, bpmnElementRenderer) {
      this.message = message;
      this.bpmnElementRenderer = bpmnElementRenderer;
      throw message;
    }

    return RenderingException;
  })();


  // constructor
  function BpmnElementRenderer(baseElement) {

    if(!baseElement) {
      throw new RenderingException("Base element cannot be null");
    }

    // the bpmn base element to be rendered
    this.baseElement = baseElement;
  };

  BpmnElementRenderer.prototype.render = function(options, gfxGroup) {
    this.gfxGroup = gfxGroup;

    // create surface element if needed
    if (!gfxGroup) {
      var width = options.width ? options.width : 800;
      var height = options.height ? options.height : 600;

      gfxGroup = this.gfxGroup = gfx.createSurface(options.diagramElement, width, height).createGroup();
    }

    // baseelement might be a array of processes
    var elements = [].concat(this.baseElement);

    for (var index in elements) {
      var currentElement = this.baseElement = elements[index];
      var bounds = this.getBounds();

      if (bounds) {
        var diagramElement = query("#"+options.diagramElement)[0];
        diagramElement.style.position = "relative";

        var style = {
          position: "absolute" ,
          left: +bounds.x+"px",
          top: +bounds.y+"px",
          width : +bounds.width + "px",
          height : +bounds.height + "px"
        };

        var overlayDiv = domConstruct.create("div", {
          id : currentElement.id,
          innerHTML : options.overlayHtml,
          style: style
        },
        diagramElement);
        domClass.add(overlayDiv, "bpmnElement");
      }


      var delegate = RENDERER_DELEGATES[currentElement.type];
      if(!!delegate) {
        this.svgElement = delegate.render(this, gfxGroup);
      } else {
        console.log("Unable to render element of type ", this.baseElement.type);
      }

      // if the current element has child base elements, create the
      // subordinate renderers
      if(!!currentElement.baseElements) {
        for(var i = 0; i < currentElement.baseElements.length; i++) {
          new BpmnElementRenderer(currentElement.baseElements[i], this).render(options, gfxGroup);
        }
      }

    }

  };

  function getBoundsFromChildren(diChildren) {
    for (var index in diChildren) {
      var diChild = diChildren[index];

      if (/bounds/i.test(diChild.type)) {
        return diChild;
      }

    }
    return null;
  };

  BpmnElementRenderer.prototype.getLabelBounds = function () {
    if (!this.baseElement.bpmndi) {
      return null;
    }

    var diChildren = this.baseElement.bpmndi[0].children;

    for (var index in diChildren) {
      var diChild = diChildren[index];

      if (/BPMNLabel/i.test(diChild.type)) {
        return getBoundsFromChildren(diChild.children);
      }

    }
    return null;
  };

  BpmnElementRenderer.prototype.getBounds = function() {
    if (!this.baseElement.bpmndi) {
      return null;
    }

    var diChildren = this.baseElement.bpmndi[0].children;

    var bounds = getBoundsFromChildren(diChildren);

    if (!bounds) {
      return;
    }

    //FIXME move this
    var currentCanvasDimension = this.getSurface().getDimensions();

    var boundsWidth = +bounds.x + +bounds.width;
    var boundsHeight = +bounds.y + +bounds.height;
    var padding = 50;

    if ( boundsWidth > currentCanvasDimension.width || boundsHeight >  currentCanvasDimension.height) {
      this.getSurface().setDimensions(boundsWidth + padding, boundsHeight + padding);
    }

    return bounds;
  };

  BpmnElementRenderer.prototype.getWaypoints = function() {
    var waypoints = [];

    var diChildren = this.baseElement.bpmndi[0].children;
    for (var index in diChildren) {
      var diChild = diChildren[index];

      if (/waypoint/i.test(diChild.type)) {
        waypoints.push({x: +diChild.x, y: +diChild.y});
      }

    }
    return waypoints;
  };

  BpmnElementRenderer.prototype.getStyle = function () {
    return styleMap[this.baseElement.type];
  };

  BpmnElementRenderer.prototype.getEventType = function () {
    var sCatch = "catch";
    var sThrow = "throw";

    switch (this.baseElement.type) {
      case "startEvent":
        return sCatch;
      case "endEvent":
        return sThrow;
      case "intermediateCatchEvent":
        return sCatch;
      case "intermediateThrowEvent":
        return sThrow;
    }
    return undefined;
  };

  BpmnElementRenderer.prototype.getSurface = function () {
    var parent = this.gfxGroup.getParent();

    do {
      if (parent instanceof dojox.gfx.Surface) {
        return parent;
      }
    }while (parent = parent.getParent())

    return null;
  };

  BpmnElementRenderer.prototype.RENDERER_DELEGATES = RENDERER_DELEGATES;

  return BpmnElementRenderer;
});
