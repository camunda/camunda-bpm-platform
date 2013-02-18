var eventDefinitions = {
			"messageCatch":" M7 10  L7 20  L23 20  L23 10  z M7 10  L15 16  L23 10 ",
			"messageThrow":"M7 9  L15 15  L23 9  z M7 10  L7 20  L23 20  L23 10  L15 16  z",
			"timer":" M15 5  L15 8  M20 6  L18.5 9  M24 10  L21 11.5  M25 15  L22 15  M24 20  L21 18.5  M20 24  L18.5 21  M15 25  L15 22  M10 24  L11.5 21  M6 20  L9 18.5  M5 15  L8 15  M6 10  L9 11.5  M10 6  L11.5 9  M17 8  L15 15  L19 15 ",
			"error": " M21.820839 10.171502  L18.36734 23.58992  L12.541380000000002 13.281818999999999  L8.338651200000001 19.071607  L12.048949000000002 5.832305699999999  L17.996148000000005 15.132659  L21.820839 10.171502  z",
			"escalation": "M15 7.75  L21 22.75  L15 16  L9 22.75  z",
			"signal": "M7.7124971 20.247342  L22.333334 20.247342  L15.022915000000001 7.575951200000001  L7.7124971 20.247342  z",
			"cancel": " M6.283910500000001 9.27369  L9.151395 6.4062062  L14.886362000000002 12.141174  L20.621331 6.4062056  L23.488814 9.273689  L17.753846 15.008657  L23.488815 20.743626  L20.621331 23.611111  L14.886362000000002 17.876142  L9.151394 23.611109  L6.283911000000001 20.743625  L12.018878 15.008658  L6.283910500000001 9.27369  z",
			"conditional": " M6 6  L24 6 L24 24 L6 24 L6 6 M9 9  L21 9  M9 13  L21 13  M9 17  L21 17  M9 21  L21 21 z",
			"compensate": "M14 8 L14 22 L7 15 L14 8 M21 8 L21 22 L14 15 L21 8 z",
			"multipleParallel": "M5.75 12  L5.75 18  L12 18  L12 24.75  L18 24.75  L18 18  L24.75 18  L24.75 12  L18 12  L18 5.75  L12 5.75  L12 12  z",
			"multiple": " M19.834856 21.874369  L9.762008 21.873529  L6.650126 12.293421000000002  L14.799725 6.373429600000001  L22.948336 12.294781  L19.834856 21.874369  z",
			"link": "M9 13 L18 13 L18 10 L23 15 L18 20 L18 17 L8 17 L8 13"
			}
			
var taskDefinitions = {
			// Public Domain: http://thenounproject.com/noun/user/#icon-No1331
			"user": "M60.541,28.82c0.532,2.353,1.176,4.893,1.301,7.342c0.033,0.654,0.072,1.512-0.201,2.07  c2.227,1.482,1.137,4.562-0.166,6.129c-0.469,0.562-1.535,1.26-1.773,1.957c-0.352,1.025-0.787,2.031-1.408,2.938  c-0.519,0.756-0.408,0.184-0.925,1.344c-0.35,1.576-0.881,5.145-0.13,6.61c0.986,1.921,3.146,3.137,4.934,4.159  c2.37,1.356,5.018,2.351,7.549,3.362c2.33,0.931,4.76,1.626,7.002,2.764c0.703,0.356,1.412,0.704,2.078,1.128  c0.537,0.342,1.438,0.869,1.566,1.559v5.424h-60.01l0.041-5.424c0.128-0.689,1.029-1.217,1.566-1.559  c0.666-0.424,1.375-0.771,2.078-1.128c2.242-1.138,4.673-1.833,7.002-2.764c2.531-1.012,5.178-2.006,7.549-3.362  c1.787-1.022,3.947-2.238,4.933-4.159c0.752-1.466,0.332-5.05-0.019-6.624l0,0c-0.601-0.389-1.016-1.594-1.357-2.197  c-0.359-0.637-0.648-1.324-1.086-1.914c-0.597-0.805-1.592-1.182-2.242-1.936c-0.434-0.502-0.619-1.124-0.834-1.74  c-0.257-0.736-0.131-1.334-0.246-2.161c-0.051-0.354,0.13-0.765,0.34-1.064c0.258-0.368,0.728-0.44,0.847-0.906  c0.147-0.577-0.177-1.253-0.239-1.823c-0.066-0.609-0.224-1.58-0.221-2.191c0.01-2.217-0.4-4.217,1.375-5.969  c0.624-0.614,1.333-1.145,2.01-1.699l0,0c0.26-0.828,1.507-1.338,2.236-1.616c0.947-0.36,1.943-0.562,2.914-0.851  c2.93-0.873,6.297-0.78,8.866,1.029c0.843,0.594,2.005,0.084,2.893,0.594C59.619,26.634,60.639,27.771,60.541,28.82z",
			// Public Domain: http://thenounproject.com/noun/gear/#icon-No1329
			"service": "M95.784,59.057c1.867,0,3.604-1.514,3.858-3.364c0,0,0.357-2.6,0.357-5.692c0-3.092-0.357-5.692-0.357-5.692  c-0.255-1.851-1.991-3.364-3.858-3.364h-9.648c-1.868,0-3.808-1.191-4.31-2.646s-1.193-6.123,0.128-7.443l6.82-6.82  c1.32-1.321,1.422-3.575,0.226-5.01L80.976,11c-1.435-1.197-3.688-1.095-5.01,0.226l-6.82,6.82c-1.32,1.321-3.521,1.853-4.888,1.183  c-1.368-0.67-5.201-3.496-5.201-5.364V4.217c0-1.868-1.514-3.604-3.364-3.859c0,0-2.6-0.358-5.692-0.358s-5.692,0.358-5.692,0.358  c-1.851,0.254-3.365,1.991-3.365,3.859v9.648c0,1.868-1.19,3.807-2.646,4.31c-1.456,0.502-6.123,1.193-7.444-0.128l-6.82-6.82  C22.713,9.906,20.459,9.804,19.025,11L11,19.025c-1.197,1.435-1.095,3.689,0.226,5.01l6.819,6.82  c1.321,1.321,1.854,3.521,1.183,4.888s-3.496,5.201-5.364,5.201H4.217c-1.868,0-3.604,1.514-3.859,3.364c0,0-0.358,2.6-0.358,5.692  c0,3.093,0.358,5.692,0.358,5.692c0.254,1.851,1.991,3.364,3.859,3.364h9.648c1.868,0,3.807,1.19,4.309,2.646  c0.502,1.455,1.193,6.122-0.128,7.443l-6.819,6.819c-1.321,1.321-1.423,3.575-0.226,5.01L19.025,89  c1.435,1.196,3.688,1.095,5.009-0.226l6.82-6.82c1.321-1.32,3.521-1.853,4.889-1.183c1.368,0.67,5.201,3.496,5.201,5.364v9.648  c0,1.867,1.514,3.604,3.365,3.858c0,0,2.599,0.357,5.692,0.357s5.692-0.357,5.692-0.357c1.851-0.255,3.364-1.991,3.364-3.858v-9.648  c0-1.868,1.19-3.808,2.646-4.31s6.123-1.192,7.444,0.128l6.819,6.82c1.321,1.32,3.575,1.422,5.01,0.226L89,80.976  c1.196-1.435,1.095-3.688-0.227-5.01l-6.819-6.819c-1.321-1.321-1.854-3.521-1.183-4.889c0.67-1.368,3.496-5.201,5.364-5.201H95.784  z M50,68.302c-10.108,0-18.302-8.193-18.302-18.302c0-10.107,8.194-18.302,18.302-18.302c10.108,0,18.302,8.194,18.302,18.302  C68.302,60.108,60.108,68.302,50,68.302z",
			"script": "M6.402,0.5h14.5c0,0-5.833,2.833-5.833,5.583s4.417,6,4.417,9.167    s-4.167,5.083-4.167,5.083H0.235c0,0,5-2.667,5-5s-4.583-6.75-4.583-9.25S6.402,0.5,6.402,0.5z"
			}

var activityMarkers = {
			"loop": "M 0 0 L 0 3 L -3 3 M 0 3 A 4.875,4.875 0 1 1 4 3",
			"miSeq": "M 0 -2 h10 M 0 2 h10 M 0 6 h10",
			"miPar": "M 0 -2 v8 M 4 -2 v8 M 8 -2 v8",
			"adhoc": "m 0 0 c -0.54305,0.60192 -1.04853,1.0324 -1.51647,1.29142 -0.46216,0.25908 -0.94744,0.38857 -1.4558,0.38857 -0.57194,0 -1.23628,-0.22473 -1.99307,-0.67428 -0.0577,-0.0306 -0.10111,-0.0534 -0.12999,-0.0687 -0.0346,-0.0228 -0.0896,-0.0533 -0.16464,-0.0915 -0.80878,-0.47234 -1.4558,-0.70857 -1.94107,-0.70857 -0.46217,0 -0.91566,0.14858 -1.36047,0.44576 -0.44485,0.2895 -0.92434,0.75046 -1.43849,1.38285 l 0,-2.03429 c 0.54881,-0.60194 1.05431,-1.0324 1.51647,-1.29147 0.46793,-0.26666 0.9532,-0.39999 1.45581,-0.39999 0.57191,0 1.24205,0.22856 2.01039,0.68574 0.0461,0.0308 0.0838,0.0533 0.11266,0.0687 0.0404,0.0228 0.0982,0.0533 0.1733,0.0913 0.803,0.4724 1.45002,0.70861 1.94108,0.70857 0.44481,4e-5 0.88676,-0.14475 1.32581,-0.43429 0.43905,-0.2895 0.9272,-0.75425 1.46448,-1.39428",
			"compensate": "M 50 70 L 55 65 L 55 75z M44.7 70 L49.7 75 L 49.7 65z"
			}
			
var regularStroke = "grey";
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
    "stroke-width": 1.5,
	"fill": "white"
};

var endEventStyle = {
    "stroke-width": 3,
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
  
  var gatewayMarkerStyle = {
    stroke: regularStroke,
	"stroke-opacity" : 1,
    "stroke-width": 4
  };

  
  var sequenceFlowStyle = {
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
	"font-family": "Arial, Helvetica, sans-serif",
  }

  var textBigStyle = {
	"font-size": 20, 
	"font-family": "Arial, Helvetica, sans-serif",
  }
  
  var caBpmnPapers = {};
  
  function bpmn (diagram, container) {

	var paper = Raphael(container.get(0), "100%");

	$.get("http://localhost:8000/app/assets/bpmn/" + diagram + ".bpmn", function(data){
		parseBpmnXml(data, paper, container);
	});

	caBpmnPapers[container.get(0).id] = paper;

	}

  function bpmnDirect (xml, container) {

	var paper = Raphael(container.get(0), "100%");
	parseBpmnXml(xml, paper, container);

	return paper;
	}	

/**
* draw single BPMN Symbol. Just a helper for camunda.org BPMN Tutorial
**/
  function drawBpmnSymbol (type, name, container) {

	var element = new Object;
	element.type = type;
	element.id = "1";

	// Just a workaround to allow linebreaks in Symbol names, does not work with "\n" directly, can't figure out why
	if (name) element.name = name.replace("<br>", "\n");
	
	element.x = 5;
	element.y = 5;
	
	var paperBufferX = 10;
	var paperBufferY = 10;
	
	if (type == "task" || type == "subprocess" || type == "callactivity" || type == "eventsubprocess" || type == "transaction" ) {
		element.width=100;
		element.height=80;
		element.collapsed=true;
		if (type == "eventsubprocess") {
			element.type = "subprocess";
			element.triggeredByEvent = true;
		}
	} else 
	if (element.type == "participant") {
		element.width=200;
		element.height=80;	
	} else
	if (element.type == "lane") {
		element.width=176;
		element.height=80;	
	} else

	if (element.type.indexOf("gateway") >= 0) {
		element.width=40;
		element.height=40;	
		element.isMarkerVisible = "true";
		element.labelX = element.x + element.width/2;
		element.labelY = element.y + element.height + 10;
		paperBufferY = 20;
		paperBufferX = 20;
	} else
	if (element.type == "dataobject") {
		element.width=50;
		element.height=60;
		element.labelX = 30;
		element.labelY = 35;
		paperBufferY = 40;
	} else	
	if (element.type == "datastorereference") {
		element.width=60;
		element.height=65;	
		element.labelX = 35;
		element.labelY = 45;
		paperBufferY = 50;
	} else	
	if (element.type == "textannotation") {
		element.textAnnotation = element.name;
		element.width=60;
		element.height=60;	
		paperBufferY = 50;
	} else	
	if (element.type == "group") {
		element.width=100;
		element.height=60;	
	} else	
	if (element.type.indexOf("event") >= 0) {
		element.width=30;
		element.height=30;	
		
		// has eventtype-definition?
		if (element.type.indexOf("/") >= 0) {
			element.eventType = element.type.substring(element.type.indexOf("/") + 1, element.type.length);
			// is non-interrupting?
			if (element.eventType.indexOf("-non") >= 0) {
				element.cancelActivity = false;
				element.eventType = element.eventType.replace("-non", "");
			
			}
		}		
	}

	var paper = Raphael(container.get(0), element.width+paperBufferX, element.height+paperBufferY);	
	
	var raphaelElementId = elementSVG(element, paper);
	
	makeHoverEffect (container, element, paper, raphaelElementId);
	
	}	
	
function makeHoverEffect (container, element, paper, raphaelElementId) {
	// Position DIV Element (if exists)
	$(container).find('#' + element.id).each(function() {
		//alert ($(this).text());
		$(this).css({
			"position": "absolute",
			"width": element.width + "px",
			"height": element.height + "px",
			"left": element.x + "px",
			"top": element.y + "px",
			"background": "url(/app/assets/img/transparent.gif) repeat" // I need this workaround to make hover effect work in IEx :-(
			});
		
		// Get Raphael Element
		var r = paper.getById(raphaelElementId);

		// Give a glow effect for node that is associated with div layer that is hovered
		$(this).hover(
		  function () {
				r.g = r.glow({
					color: highlightStroke, 
					width: 10
				});
		  }, 
		  function () {
				r.g.remove();
		  }
		);
		
		// mark nodes that can be hovered with colored border
		r.attr({stroke: highlightStroke});
		
	});
}
	
// For line breaking the caption in activities
function textLineBreaker (t, content, maxWidth) {
	// set some buffer
	maxWidth = maxWidth-5;
	
	// content exists?
	if (content) {
		var words = content.split(" ");

		var tempText = "";
		for (var i=0; i<words.length; i++) {
		  t.attr("text", tempText + " " + words[i]);
		  if (t.getBBox().width > maxWidth) {
			tempText += "\n" + words[i];
		  } else {
			tempText += " " + words[i];
		  }
		}
		t.attr("text", tempText.substring(1));
	}
}

function elementSVG (element, paper) {
	var drawnElement;
	// Event?
	if ((element.type.toLowerCase().indexOf("event") >= 0) && (element.type.toLowerCase().indexOf("gateway") == -1)) {
		var rad = element.width / 2;	
		var x = element.x + rad;
		var y = element.y + rad;
		
		// event border
		drawnElement = paper.circle(x, y, rad)
			  .attr(generalStyle).attr(eventStyle);
			  
		// mark as non-interrupting if necessary
		if (element.cancelActivity == false) {
			drawnElement.attr({"stroke-dasharray": "-"})
		}
		
		// intermediate or boundary?
		if ((element.type.toLowerCase().indexOf("intermediate") >= 0) || (element.type.toLowerCase().indexOf("boundary") >= 0)) {
			// intermediate event border
			var innerCircle = paper.circle(x, y, rad-2)
			  .attr(generalStyle).attr(eventStyle).attr({fill:"none"});
			// mark as non-interrupting if necessary
			if (element.cancelActivity == false) {
				innerCircle.attr({"stroke-dasharray": "-"})
			}
		
		// end?
		} else if (element.type.toLowerCase().indexOf("end") >= 0) {
			// end event border
			drawnElement.attr(endEventStyle);
			// adjust radius due to thicker border (rad = stroke-width/2)
			// rad = rad + 0.75; not working as intended...
		}
		
		
		// message?
		if (element.eventType == "message") {
			// catch?
			if ((element.type.toLowerCase().indexOf("catch") >= 0)|| (element.type.toLowerCase().indexOf("boundary") >= 0) || (element.type.toLowerCase().indexOf("start") >= 0)) {
				var myPathSpec = eventDefinitions["messageCatch"];
				var myPath = paper.path(myPathSpec).attr(generalStyle).attr(eventStyle);
				myPath.translate(x - rad, y-rad);
			} else {
				var myPathSpec = eventDefinitions["messageThrow"];
				var myPath = paper.path(myPathSpec).attr(generalStyle).attr({"stroke":"none", "fill":regularStroke});
				myPath.translate(x - rad, y-rad);
			}
		// timer?
		} else if (element.eventType == "timer") {
			paper.circle(x, y, 10).attr(generalStyle).attr(eventStyle);
			var myPathSpec = eventDefinitions[element.eventType];
			var myPath = paper.path(myPathSpec).attr(generalStyle).attr(eventStyle);
			myPath.translate(x - rad, y-rad);
		// terminate?
		} else if (element.eventType == "terminate") {
			paper.circle(x, y, 8).attr(generalStyle).attr(eventStyle).attr({"fill":regularStroke});
		// cancel?
		} else if (element.eventType == "cancel") {
			var myPathSpec = eventDefinitions[element.eventType];
			var myPath = paper.path(myPathSpec).attr(generalStyle).attr(eventStyle);
			myPath.translate(x - rad, y-rad);
			// throwing?
			if ((element.type.toLowerCase().indexOf("throw") >= 0) || element.type.toLowerCase().indexOf("end") >= 0){
				myPath.attr({"stroke":"none", "fill":regularStroke});
			}
		// sth. else?
		} else if ((element.eventType == "error") || (element.eventType == "multipleParallel") || (element.eventType == "multiple") || (element.eventType == "escalation") || (element.eventType == "link") || (element.eventType == "signal") || (element.eventType == "cancel") || (element.eventType == "conditional") || (element.eventType == "compensate")) {
			var myPathSpec = eventDefinitions[element.eventType];
			var myPath = paper.path(myPathSpec).attr(generalStyle).attr(eventStyle);
			myPath.translate(x - rad, y-rad);
			// throwing?
			if ((element.type.toLowerCase().indexOf("throw") >= 0) || element.type.toLowerCase().indexOf("end") >= 0){
				myPath.attr({"stroke":"none", "fill":regularStroke});
			}
		}

		if (element.name) { 
			if (element.labelX) {
				paper.text(element.labelX, element.labelY, element.name).attr(textStyle).attr({'text-anchor': 'start'});
			} else {
				paper.text(x, parseInt(y) + parseInt(element.height/2) + 15, element.name).attr(textStyle);
			}
		}
		
	// Gateways
	} else if (element.type.toLowerCase().indexOf("gateway") >= 0) {
		var x = element.x;
		var y = parseInt(element.y) + element.height/2;
		var radHeight = element.height/2;
		var radWidth = element.width/2;
		var rhombus = "M" + x + " " + y + " l" + radWidth + " -" + radHeight + " l" + radWidth + " " + radHeight + " l-" + radWidth + " " + radHeight + " l-" + radHeight + " -" + radWidth;
		drawnElement = paper.path(rhombus)
				.attr(generalStyle).attr(gatewayStyle);
		
		if (element.name) {
			if (element.labelX) {
				paper.text(element.labelX, element.labelY, element.name).attr(textStyle).attr({'text-anchor': 'start'});
			} else {
				paper.text(parseInt(x) + parseInt(element.width) - 5, parseInt(y) + parseInt(element.height/2) -4, element.name).attr(textStyle).attr({'text-anchor': 'start'});
			}
		}

		// Exclusive?
		if (element.type == "exclusivegateway") {
			// Should marker be visible?
			if (element.isMarkerVisible == "true") {
			var myPathSpec = " M13.25 12.05  L17.25 12.05  L27.65 28.95  L23.75 28.95  z";
			var myPath = paper.path(myPathSpec).attr(gatewayMarkerStyle).attr({"stroke-width":"1", "fill":regularStroke});
			myPath.translate(x, y - radHeight);

			myPathSpec = " M13.25 28.95  L23.75 12.05  L27.65 12.05  L17.25 28.95  z";
			myPath = paper.path(myPathSpec).attr(gatewayMarkerStyle).attr({"stroke-width":"1", "fill":regularStroke});
			myPath.translate(x, y - radHeight);
			}

		// Parallel?
		} else if (element.type == "parallelgateway") {
			var myPathSpec = " M11.25 20.5  L30.25 20.5  M20.5 11.25  L20.5 30.25 ";
			var myPath = paper.path(myPathSpec).attr(gatewayMarkerStyle);
			myPath.translate(x, y - radHeight);

		// Inclusive?
		} else if (element.type == "inclusivegateway") {
			var rad = element.width / 3.5;	
			var x = element.x + element.width/2;
			var y = element.y + element.height/2;
			paper.circle(x, y, rad-2).attr(gatewayMarkerStyle).attr({"stroke-width":3});
					
		// Event based?
		} else if (element.type == "eventbasedgateway") {
			var rad = element.width / 3.3;	
			var x = element.x + element.width/2;
			var y = element.y + element.height/2;
			paper.circle(x, y, rad).attr(gatewayMarkerStyle).attr({"stroke-width":1});
			paper.circle(x, y, rad-2).attr(gatewayMarkerStyle).attr({"stroke-width":1});
			
			var pathSpec = "M24.827514 26.844972  L15.759248000000001 26.844216  L12.957720300000002 18.219549  L20.294545 12.889969  L27.630481000000003 18.220774  L24.827514 26.844972  z";
			var myPath = paper.path(pathSpec).attr(generalStyle).attr({"stroke-width":1.5});
			myPath.translate(element.x , element.y);

		// Complex?
		} else if (element.type == "complexgateway") {
			var pathSpec = "M10.75 20.5  L30.25 20.5  M20.5 10.75  L20.5 30.25  M13.35 13.35  L27.65 27.65  M13.35 27.65  L27.65 13.35";
			var myPath = paper.path(pathSpec).attr(generalStyle);
			myPath.translate(element.x , element.y);
		}		
	
		
	// Pools
	} else if (element.type.toLowerCase().indexOf("participant") >= 0) {
		drawnElement = paper.rect(
				element.x, 
				element.y, 
				element.width, 
				element.height, 
				0)
			  .attr(generalStyle);
		
		
		// if collapsed, make a biiig caption
		if (element.collapsed == true) {
			var textX = element.x + element.width/2;
			var textY = element.y + element.height/2;
			if (element.name) paper.text(textX, textY, element.name).attr(textBigStyle);			  
	
		} else  {
			var textX = parseInt(parseInt(element.x) + 10);
			var textY = parseInt(parseInt(element.y) + parseInt(element.height)/2);
			if (element.name) paper.text(textX, textY, element.name).rotate(-90,x,200).attr(textStyle);			  
		}
		
	// Lanes
	} else if (element.type.toLowerCase().indexOf("lane") >= 0) {
		drawnElement = paper.rect(
				element.x, 
				element.y, 
				element.width, 
				element.height, 
				0)
			  .attr(generalStyle);
		var textX = parseInt(parseInt(element.x) + 10);
		var textY = parseInt(parseInt(element.y) + parseInt(element.height)/2);
		if (element.name) paper.text(textX, textY, element.name).rotate(-90,x,200).attr(textStyle);			  

	// Text Annotation?
	} else if (element.type.toLowerCase().indexOf("textannotation") >= 0) {

		// Drawing the Shape
		var x = parseInt(element.x) + 10;
		var y = element.y;
		var height = element.height;
		var pathSpec = "M" + x + " " + y + " l-10 0 l0 " + element.height + " l10 0";
		drawnElement = paper.path(pathSpec).attr(generalStyle);
		
		// Printing the Text
		var textX = parseInt(parseInt(element.x) + 5);
		var textY = parseInt(parseInt(element.y) + parseInt(element.height)/2);
		var t = paper.text(textX, textY, element.textAnnotation).attr(textStyle).attr({'text-anchor': 'start'});			  
		textLineBreaker (t, element.textAnnotation, element.width);
		
	// Group?
	} else if (element.type == "group") {

		drawnElement = paper.rect(
				element.x, 
				element.y, 
				element.width, 
				element.height, 
				5)
			  .attr(groupStyle).attr({"stroke-dasharray":"- ."});

		var textX = element.x + 5;
		var textY = element.y + 10;
		if (element.name) var t = paper.text(textX, textY, element.name).attr(textStyle).attr({'text-anchor': 'start'});	

	// DataObject?
	} else if (element.type == "dataobject") {
		var pathSpec = "M" + element.x + " " + element.y + " l" + (element.width-10) + " 0 l10 10 l0 " + (element.height-10) + " l-" + element.width + " 0 l0 -" + element.height + "M" + (element.x + element.width - 10) + " " + element.y + " l0 10 l10 0";
		drawnElement = paper.path(pathSpec).attr(generalStyle);

		if (element.name) { 
			if (element.labelX) {
				paper.text(element.labelX, element.labelY, element.name).attr(textStyle);
			} else {
				paper.text(element.x + element.width/2, element.y + element.height + 15, element.name).attr(textStyle);
			}
		}		

	// DataStore?
	} else if (element.type == "datastorereference") {
		var pathSpec = "M30.708999999999985 0  c20.013 0 31.292 3.05 31.292 5.729  c0 2.678 0 45.096 0 48.244  c0 3.148 -16.42 6.2 -31.388 6.2  c-14.968 0 -30.613 -2.955 -30.613 -6.298  c0 -3.342 0 -45.728 0 -48.05  C-1.4210854715202004e-14 3.503 10.696999999999985 0 30.708999999999985 0  M62.00099999999999 15.027999999999999  c0 1.986 -3.62 6.551 -31.267 6.551  c-27.646 0 -30.734 -4.686 -30.734 -6.454  M-1.4210854715202004e-14 10.475000000000001  c0 1.769 3.088 6.455 30.734 6.455  c27.647 0 31.267 -4.565 31.267 -6.551  M-1.4210854715202004e-14 5.825000000000001  c0 2.35 3.088 6.455 30.734 6.455  c27.647 0 31.267 -3.912 31.267 -6.552  M62.00099999999999 5.729000000000001  v4.844  M0.0239999999999857 5.729000000000001  v4.844  M62.00099999999999 10.379000000000001  v4.844  M0.0239999999999857 10.379000000000001  v4.844 ";

		drawnElement = paper.path(pathSpec).attr(generalStyle);
		drawnElement.translate(element.x, element.y);
		
		if (element.name) { 
			if (element.labelX) {
				paper.text(element.labelX, element.labelY, element.name).attr(textStyle);
			} else {
				paper.text(element.x + element.width/2, element.y + element.height + 15, element.name).attr(textStyle);
			}
		}		
		
	// Tasks 
	} else if (element.type.toLowerCase().indexOf("task") >= 0)  {

		drawnElement = paper.rect(
				element.x, 
				element.y, 
				element.width, 
				element.height, 
				5)
			  .attr(activityStyle);

		var textX = parseInt(parseInt(element.x) + parseInt(element.width)/2);
		var textY = parseInt(parseInt(element.y) + parseInt(element.height)/2);
		if (element.name) {
			var t = paper.text(textX, textY, element.name).attr(textStyle);	
			textLineBreaker (t, element.name, element.width);
		}
		
		var taskType = element.type.replace("task", "");
		
		if (taskType == "user") {
			var pathSpec = taskDefinitions[taskType];
			var drawnTaskType = paper.path(pathSpec).attr(generalStyle).attr({"stroke-width":1, "fill":regularStroke});
			drawnTaskType.translate(element.x-element.width/2 + 13, element.y-element.height/2 + 1);
			drawnTaskType.scale(0.25,0.25);
			
		} else if (taskType == "service") {
			var pathSpec = taskDefinitions[taskType];
			drawnTaskType = paper.path(pathSpec).attr(generalStyle).attr({"stroke-width":1, "fill":regularStroke});
			drawnTaskType.translate(element.x-element.width/2 + 13, element.y-element.height/2 + 1);
			drawnTaskType.scale(0.15,0.15);
		} else if (taskType == "manual") {
			var pathSpec1 = "M0.5,3.751l4.083-3.25c0,0,11.166,0.083,12.083,0.083s-2.417,2.917-1.5,2.917 s11.667,0,12.584,0c1.166,1.708-0.168,3.167-0.834,3.667s0.875,1.917-1,4.417c-0.75,0.25,0.75,1.875-1.333,3.333     c-1.167,0.583,0.583,1.542-1.25,2.833c-1.167,0-20.833,0.083-20.833,0.083l-2-1.333V3.751z";
			pathSpec1 = pathSpec1 + " M 13.5 7 L 27 7 M 13.5 11 L 26 11 M 14 14.5 L 25 14.5 M 8.2 3.1 L 15 3.1";
			drawnTaskType = paper.path(pathSpec1).attr(generalStyle).attr({"stroke-width":1, "fill":"white"});
			drawnTaskType.translate(element.x, element.y);
			drawnTaskType.scale(0.6,0.6);
		} else if (taskType == "script") {
			var pathSpec1 = "M6.402,0.5h14.5c0,0-5.833,2.833-5.833,5.583s4.417,6,4.417,9.167    s-4.167,5.083-4.167,5.083H0.235c0,0,5-2.667,5-5s-4.583-6.75-4.583-9.25S6.402,0.5,6.402,0.5z";
			pathSpec1 = pathSpec1 + " M 3.5 4.5 L 13.5 4.5 M 3.8 8.5 L 13.8 8.5 M 6.3 12.5 L 16.3 12.5 M 6.5 16.5 L 16.5 16.5";
			drawnTaskType = paper.path(pathSpec1).attr(generalStyle).attr({"stroke-width":1, "fill":"white"});
			drawnTaskType.translate(element.x, element.y);
			drawnTaskType.scale(0.6,0.6);
		} else if (taskType == "businessrule") {
			drawnTaskType = paper.rect(
					element.x + 5, 
					element.y + 4, 
					17, 
					12, 
					0)
				  .attr(activityStyle).attr({"stroke-width":1});
			drawnTaskType = paper.rect(
					element.x + 5, 
					element.y + 4, 
					17, 
					4, 
					0)
				  .attr(activityStyle).attr({"stroke-width":1, "fill":regularStroke});


			var pathSpec1 = "M 2 10 L 19 10 M 7 4 L 7 14";
			drawnTaskType = paper.path(pathSpec1).attr(generalStyle).attr({"stroke-width":1, "stroke-linecap": "butt", "stroke-linejoin": "butt"});
			drawnTaskType.translate(element.x + 3, element.y+2);
			
		} else if (taskType == "receive") {
			
			// If instantiating, draw circle around envelope
			if (element.isInstantiate == true) {
				var rad = 12;	
				var x = element.x + 15;
				var y = element.y + 15;
				
				// event border
				drawnCircle = paper.circle(x, y, rad)
					  .attr(generalStyle).attr(eventStyle).attr({"stroke-width":1});
			}

			var pathSpec1 = eventDefinitions["messageCatch"]
			drawnTaskType = paper.path(pathSpec1).attr(generalStyle).attr({"stroke-width":1, "fill":"white"});
			drawnTaskType.translate(element.x, element.y);
			
		} else if (taskType == "send") {
			var pathSpec1 = eventDefinitions["messageThrow"]
			drawnTaskType = paper.path(pathSpec1).attr(generalStyle).attr({"stroke":"none", "fill":regularStroke});
			drawnTaskType.translate(element.x-2, element.y-4);
		}
		
		// Loop Marker?
		if (element.loop) {
			var pathSpec = activityMarkers[element.loop];
			var drawnMarker = paper.path(pathSpec).attr(generalStyle);
			//drawnMarker.translate(element.x, element.y);
			drawnMarker.translate(element.x + element.width/2 - 5, element.y + element.height - 10);
			
		}
		
		// Compensation Marker?
		if (element.isForCompensation == true) {
			var pathSpec = activityMarkers["compensate"];
			var drawnMarker = paper.path(pathSpec).attr(generalStyle);
			// if also loop marker, put left to loop marker
			if (element.loop) {
				drawnMarker.translate(element.x-15, element.y);
			} else {
				drawnMarker.translate(element.x, element.y);
			}
		}		

	// Subprocesses
	} else if (element.type == "adhocsubprocess" || element.type == "subprocess" || element.type == "transaction" || element.type == "callactivity") {
		
		drawnElement = paper.rect(
				element.x, 
				element.y, 
				element.width, 
				element.height, 
				5)
			  .attr(activityStyle);

		// CallActivity? Then make thick border...
		if (element.type == "callactivity") {
			drawnElement.attr({"stroke-width":4});
		}


	  // eventSubProcess? then make a dashed border...
		if (element.triggeredByEvent == true) {
			drawnElement.attr({"stroke-dasharray":"-","stroke-width":1});
		}

		
		// transaction? Then draw second border...
		if (element.type == "transaction") {
			drawnElement = paper.rect(
					element.x + 3, 
					element.y + 3, 
					element.width - 6, 
					element.height - 6, 
					3)
				  .attr(activityStyle);
		}

			  
		// collapsed? Then draw the cross...
		if (element.collapsed == true) {
			paper.rect(
				element.x + element.width/2 -6, 
				element.y + element.height - 12, 
				12, 
				12, 
				0)
			  .attr(activityStyle).attr({"fill":"white"});		
			  
			var pathSpec = "M50 71 v6 M 47 74 h6";
			var drawnCross = paper.path(pathSpec).attr(generalStyle);
			drawnCross.translate(element.x, element.y);
		} else {
		// if expanded, fill transparent so you can see what's inside...
			drawnElement.attr({"fill":"transparent"});
		}

	
		// Adhoc? Then draw marker...
		if (element.type == "adhocsubprocess") {
			var pathSpec = activityMarkers["adhoc"];
			var drawnMarker = paper.path(pathSpec).attr(generalStyle);
			drawnMarker.translate(element.x + element.width/2 + 10, element.y + element.height - 10);
		}
		
		// Loop Marker?
		var loopMarker;
		if (element.loop) {
			var pathSpec = activityMarkers[element.loop];
			loopMarker = paper.path(pathSpec).attr(generalStyle);
			var shiftX = 0;
			// shift left in case of other markers
			if (element.collapsed == true) shiftX = shiftX - 17;
			if (element.type == "adhocsubprocess") shiftX = shiftX - 17;
			loopMarker.translate(element.x + element.width/2 + shiftX, element.y + element.height - 10);
		}
		
		// Compensation Marker?
		if (element.isForCompensation == true) {
			var pathSpec = activityMarkers["compensate"];
			var drawnMarker = paper.path(pathSpec).attr(generalStyle);
			// if also loop marker, put left to loop marker
			if (loopMarker) {
				drawnMarker.translate(element.x + loopMarker.attr("cx") - 30, element.y);
			} else {
				drawnMarker.translate(element.x, element.y);
			}
		}		

		if (element.name) {
			// collapsed? Then place caption in the middle
			if (element.collapsed == true) {
				var textX = element.x + element.width/2;
				var textY = element.y + element.height/2;
				var t = paper.text(textX, textY, element.name).attr(textStyle);	
			} else {
			// otherwise place caption top left
				var textX = element.x + 8;
				var textY = element.y + 15;
				var t = paper.text(textX, textY, element.name).attr(textStyle).attr({'text-anchor': 'start'});	
			}
			textLineBreaker (t, element.name, element.width);
		}
	
	
	// anything else..
	} else {
		alert ("Not recognized: '" + element.name + "' of type '" + element.type + "'\n\ndrawing red rectangle as placeholder");
		
		drawnElement = paper.rect(
				element.x, 
				element.y, 
				element.width, 
				element.height, 
				5)
			  .attr(activityStyle).attr({"stroke":"red"});

		var textX = parseInt(parseInt(element.x) + parseInt(element.width)/2);
		var textY = parseInt(parseInt(element.y) + parseInt(element.height)/2);
		if (element.name) {
			var t = paper.text(textX, textY, element.name).attr(textStyle);	
			textLineBreaker (t, element.name, element.width);
		}
		
	}	
	
	drawnElement.node.id = "svg_" + element.id;
	drawnElement.id = element.id;

	drawnElement.data ("bpmnType", element.type);
	return drawnElement.id;
}

function drawFlow (flow, pathSpec, paper) {
    var drawnFlow;
	var pathString = "M"+(pathSpec[0].x)+","+(pathSpec[0].y);
    for (var i=1; i<pathSpec.length; i++) { 
      if(i==1) {
        pathString += "L";
      }
      pathString += (pathSpec[i].x)+","+(pathSpec[i].y);
      if((i+1) != pathSpec.length) {
        pathString += " ";
      }
    }
	
	if (flow.type == "sequenceflow") { 
		// draw sequenceflow 
		drawnFlow = paper.path(pathString).attr(generalStyle).attr(sequenceFlowStyle),
			l = drawnFlow.getTotalLength(),
		   to = 1;
		
	}
	
	if (flow.type == "messageflow") { 
		// draw messageFlow 
		drawnFlow = paper.path(pathString).attr(generalStyle).attr(messageFlowStyle),
			l = drawnFlow.getTotalLength(),
		   to = 1;
		var circle = paper.circle(pathSpec[0].x, pathSpec[0].y, 4).attr(generalStyle).attr({"fill":"white"});
	}	
	
	if (flow.type == "association" || flow.type == "dataAssociation") { 
		drawnFlow = paper.path(pathString).attr({"stroke-dasharray":". "}),
			l = drawnFlow.getTotalLength(),
		   to = 1;
	}

	if (flow.type == "datainputassociation" || flow.type == "dataoutputassociation") { 
		drawnFlow = paper.path(pathString).attr({"stroke":regularStroke, "stroke-width":2, "stroke-dasharray":". ", "arrow-end": "classic-wide-long"}),
			l = drawnFlow.getTotalLength(),
		   to = 1;
	}

	
	// Print the Text
	if (flow.name != undefined && flow.name != "") { 
		var textX = 0;
		var textY = 0;
		// going from left to right? (asking for more than 1px Difference due to possible inaccurateness in XML DI)
		if ((pathSpec[1].x - pathSpec[0].x) > 1) {
			textX = parseInt(pathSpec[0].x) + ((pathSpec[1].x - pathSpec[0].x) / 4);
			//alert (textX);
			textY = parseInt(pathSpec[0].y) - 10;
		// going from top to bottom / bottom to top...
		} else {
			textX = parseInt(pathSpec[0].x) + 5;
			textY = parseInt(pathSpec[0].y) + ((pathSpec[1].y - pathSpec[0].y) / 4);
		}
		
		paper.text(textX, textY, flow.name).attr({'text-anchor': 'start'});
	}

	drawnFlow.id = flow.id;

}

function drawElement (element, elemXML, paper, container, xmlJQuery) {
	var raphaelElementId;

		// if Textannotation, get Text
		if (element.type == "textannotation") {
			element.textAnnotation = elemXML.find("text").text();
		}
	
		// if Pool, determine whether it is collapsed
		if (element.type == "participant") {
			if ($(elemXML).attr("processRef")) {
				element.collapsed = false;
			} else {
				element.collapsed = true;
			}
		}

		// if Subprocess, determine whether it is eventSubProcess
		if (element.type == "subprocess") {
			if ($(elemXML).attr("triggeredByEvent") == "true") {
				element.triggeredByEvent = true;
			} else {
				element.triggeredByEvent = false;
			}
		}

		// if Task or Subprocess, determine loop/MI/Compensation Marker
		if (element.type == "adhocsubprocess" || element.type == "callactivity" || element.type == "subprocess" || element.type.toLowerCase().indexOf("task") >= 0) {
			if ($(elemXML).attr("isForCompensation") == "true") {
				element.isForCompensation = true;
			} else {
				element.isForCompensation = false;
			}
			if ($(elemXML).find("standardLoopCharacteristics").length > 0) {
				element.loop = "loop";
			}
			if ($(elemXML).find("multiInstanceLoopCharacteristics").length > 0) {
				if ($(elemXML).find("multiInstanceLoopCharacteristics").attr("isSequential") == "true") {
					element.loop = "miSeq";
				} else {
					element.loop = "miPar";
				}
			}
		}

		// if ReceiveTask determine if it is instantiating
		if (element.type == "receivetask") {
			if ($(elemXML).attr("instantiate") == "true") {
				element.isInstantiate = true;
			} else {
				element.isInstantiate = false;
			}
		}
	
		// if event, determine eventType
		if ((element.type.toLowerCase().indexOf("event") >= 0) && (element.type.toLowerCase().indexOf("gateway") == -1)) {
		// Containts Event Definitions?
			$(elemXML).find("*").filter(function() {
			    return this.nodeName.toLowerCase().match(/[^\d]eventdefinition/)}).each(function() {
					// if already set, this is a muliple event
					if (element.eventType) {
						// is parallel Multiple?
						if ($(elemXML).attr("parallelMultiple") == "true") {
							element.eventType = "multipleParallel";
						} else {
							element.eventType = "multiple";
						}					
					} else {
						element.eventType = (this).nodeName.toLowerCase().replace("eventdefinition","").toLowerCase();
					}
					
					// would cancel Activity / interrupt eventSubProcess?
					if ($(elemXML).attr("cancelActivity") == "false"  || $(elemXML).attr("isInterrupting") == "false") {
						element.cancelActivity = false;
					} else {
						element.cancelActivity = true;
					}
				});
			
		}

	// Find respective DI
	var found = false;
	$(xmlJQuery).find("bpmndi\\:BPMNShape[bpmnElement='" + element.id + "'], BPMNShape[bpmnElement='" + element.id + "']").each(function(){
		found = true;
		
		var $di = $(this);
		element.x = parseFloat($(this).find('omgdc\\:Bounds, Bounds').attr("x"));
		element.y = parseFloat($(this).find('omgdc\\:Bounds, Bounds').attr("y"));
		element.width = parseFloat($(this).find('omgdc\\:Bounds, Bounds').attr("width"));
		element.height = parseFloat($(this).find('omgdc\\:Bounds, Bounds').attr("height"));
		
		// get Label Position if existing
		$(this).find("BPMNLabel").each(function() {
			element.labelX = parseFloat($(this).find("omgdc\\:Bounds, Bounds").attr("x"));
			element.labelY = parseFloat($(this).find("omgdc\\:Bounds, Bounds").attr("y"));
		});		

		// if exclusiveGateway, determine if marker should be visible
		if (element.type == "exclusivegateway") {
			element.isMarkerVisible = $(this).attr("isMarkerVisible");
		}

		// if Subprocess, determine if collapsed
		if (element.type == "adhocsubprocess" || element.type == "subprocess" || element.type == "transaction" || element.type == "callactivity"){
			if ($(this).attr("isExpanded") == "true") {
				element.collapsed = false;
			} else {
				element.collapsed = true;
			}
		}
		
			// Draw Symbol as SVG
			raphaelElementId = elementSVG (element, paper);

		});

		if (!found) console.log ("DI not found for '" + element.name + "'");

		makeHoverEffect(container, element, paper, raphaelElementId);
}

function parseBpmnXml (data, paper, container) {
	var start = new Date().getTime();

	var symbols = new Array("participant",
							"lane",
							"subprocess",
							"transaction",
							"adhocsubprocess",
							"task", 
							"sendtask",
							"receivetask",
							"usertask",
							"manualtask",
							"businessruletask",
							"scripttask",
							"servicetask",
							"callactivity",
							"exclusivegateway", 
							"inclusivegateway", 
							"parallelgateway",
							"eventbasedgateway",
							"complexgateway",
							"startevent", 
							"intermediatethrowevent", 
							"intermediatecatchevent", 
							"endevent", 
							"boundaryevent",
							"group",
							"dataobject",
							"datastorereference",
							"textannotation",
							"sequenceflow",
							"messageflow",
							"association",
							"datainputassociation",
							"dataoutputassociation"
							);
	
	
	xmlJQuery = $.parseXML(data);
	
	$(xmlJQuery).find("*").each(function() {
		var myNodeName = (this).nodeName.toLowerCase();

		// Namespaces?
		if (myNodeName.indexOf(":") >= 0) {
			myNodeName = myNodeName.substr(myNodeName.indexOf(":") +1 , myNodeName.length);
		}

		if ($.inArray(myNodeName, symbols) > -1) {
				var elem = $(this);
				var element = new Object;
				element.type = myNodeName;
				element.id = elem.attr("id");
				element.name = elem.attr("name");
				
				if (element.type == "sequenceflow" || element.type == "messageflow" || element.type == "association" || element.type == "datainputassociation" || element.type == "dataoutputassociation") {
					// Find respective DI
					var pathSpec = new Array();
					$(xmlJQuery).find("bpmndi\\:BPMNEdge[bpmnElement='" + element.id + "'], BPMNEdge[bpmnElement='" + element.id + "']").each(function(){
						var $di = $(this);
						$di.find("omgdi\\:waypoint, waypoint").each(function(){
							var waypoint = $(this);
							pathSpecElem = new Object();
							pathSpecElem.x = waypoint.attr("x");
							pathSpecElem.y = waypoint.attr("y");
							pathSpec.push(pathSpecElem);
						});
					
					drawFlow(element, pathSpec, paper);
					});
				} else {
					drawElement(element, $(this), paper, container, xmlJQuery);
				}
		}
	});
	
	// determine maxY
	var maxX = 0; // maximum X Value for Resizing Canvas later
	var maxY = 0; // maximum Y Value for Resizing Canvas later

	$(xmlJQuery).find("bpmndi\\:BPMNShape, BPMNShape").each(function(){
		myX = parseInt($(this).find("omgdc\\:Bounds, Bounds").attr("x")) + parseInt($(this).find("omgdc\\:Bounds, Bounds").attr("width"));
		if (myX > maxX) {maxX = myX;}

		myY = parseInt($(this).find("omgdc\\:Bounds, Bounds").attr("y")) + parseInt($(this).find("omgdc\\:Bounds, Bounds").attr("height"));
		if (myY > maxY) {maxY = myY;}
	});
	
	paper.setSize (maxX + 30, maxY + 30);
	var end = new Date().getTime();
	var delta = end-start;
	console.log ("BPMN successfully rendered in " + delta + " ms.");
}


