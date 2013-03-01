/////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////

/**
 * The BPMN 2.0 transformer module
 *
 * This module provides the functionality necessary to transform
 * a BPMN 2.0 XML file into a set of ActivityDefinitions that can be consumed
 * by the process engine.
 */
define([], function () {

  // XML namespaces
  var NS_BPMN_SEMANTIC = "http://www.omg.org/spec/BPMN/20100524/MODEL";
  var NS_BPMN_DIAGRAM_INTERCHANGE = "http://www.omg.org/spec/BPMN/20100524/DI";
  var NS_OMG_DC = "http://www.omg.org/spec/DD/20100524/DC";
  var NS_OMG_DI = "http://www.omg.org/spec/DD/20100524/DI";

  /** the parse listeners are callbacks that are invoked by the transformer
   * when activity definitions are created */
  var parseListeners = [];

  function getXmlObject(source) {
    // use the browser's DOM implemenation
    var xmlDoc;
    if (window.DOMParser) {
      var parser = new DOMParser();
      xmlDoc = parser.parseFromString(source,"text/xml");
    } else {
      xmlDoc = new ActiveXObject("Microsoft.XMLDOM");
      xmlDoc.async=false;
      xmlDoc.loadXML(source);
    }
    return xmlDoc;
  }

  function Transformer () {
  }

  Transformer.prototype.parseListeners = parseListeners;

  Transformer.prototype.transform =  function(source) {

    var doc = getXmlObject(source);
    var definitions = doc.getElementsByTagName("definitions");

    if(definitions.length == 0) {
      throw "A BPMN 2.0 XML file must contain at least one definitions element";
    }

    // the generated Elements
    var generatedElements = [];
    var isExecutable = false;
    var lastGeneratedId = 0;

    function createBpmnObject(element, scope, bpmnDiElementIndex) {

      var bpmnObject = {};

      if(!!scope) {
        // add it to the parent activity definition
        scope.baseElements.push(bpmnObject);
      }

      var attributes = element.attributes;

      // set the type
      bpmnObject.type = element.localName;

      // copy all attributes from the xml element to the json object
      for(var i = 0; attributes != null && i < attributes.length; i++) {
        var attribute = attributes[i];
        bpmnObject[attribute.nodeName] = attribute.nodeValue;
      }

      // TODO an we do this better?
      if (bpmnObject.type == "textAnnotation") {
        var text = element.getElementsByTagName("text")[0].firstChild.data;
        bpmnObject["text"] = text;
      }

      var bpmnDiObject = bpmnDiElementIndex[bpmnObject.id];
      if(!!bpmnDiObject) {
        bpmnObject.bpmndi = bpmnDiObject;
      }

      // generate ID if not present:
      if(!bpmnObject.id) {
        bpmnObject.id = bpmnObject.type + "_" + lastGeneratedId;
        lastGeneratedId++;
      }

      return bpmnObject;

    }

    /** creates an ActivityDefinition and adds it to the scope activity.
     * 'element' is a DOMElement
     * 'scope' is an ActivityDefinition
     */
    function createFlowElement(element, scope, sequenceFlows, bpmnDiElementIndex) {
      // the ActivityDefinition to be built

      var bpmnObject = createBpmnObject(element, scope, bpmnDiElementIndex);

      bpmnObject.outgoing = [];
      bpmnObject.listeners = [];

      var attributes = element.attributes;

      // set and validate sequenceFlows
      if(!!sequenceFlows) {
        var outgoingFlows = sequenceFlows[bpmnObject.id];
        if(!!outgoingFlows) {

          for(var i =0; i < outgoingFlows.length; i++) {
            bpmnObject.outgoing.push(outgoingFlows[i].id);
          }

          if(!!bpmnObject.default && isExecutable) {

            var conditionalFlowFound = false;

            for(var i =0; i < outgoingFlows.length; i++) {
              var sequenceFlow = outgoingFlows[i];

              if(!!sequenceFlow.condition) {

                if(bpmnObject.defaultFlowId == sequenceFlow.id) {
                  throw "Sequence flow with id '" + sequenceFlow.id + "' is configured to be the default flow but has a condition";
                } else {
                  // if a default flow is configured, there needs to be at least one conditional flow:
                  conditionalFlowFound = true;
                }
              }
            }

            if(!conditionalFlowFound) {
              throw "Activity with id '"+bpmnObject.id+"' declares default flow with id '" + bpmnObject.default + "' but has no conditional flows.";
            }
          }
        }
      }

      return bpmnObject;
    };

    function transformTask(element, scope, sequenceFlows, bpmnDiElementIndex) {
      // the ActivityDefinition to be built

      var taskObject = createFlowElement(element, scope, sequenceFlows, bpmnDiElementIndex);
      return taskObject;
    };

    function transformLaneSet(laneSetElement, scope, bpmnDiElementIndex) {
      // TODO not creating a seperate bpmn object for the lane set, adding lanes to the process directly
      var element = laneSetElement.firstChild;
      do {
        var elementType = element.nodeName;
        if (elementType == "lane") {
          createBpmnObject(element, scope, bpmnDiElementIndex);
        }
      }
      while (element = element.nextSibling)
    };


    function transformEvent(element, scope, sequenceFlows, bpmnDiElementIndex) {
      // the ActivityDefinition to be built

      var eventObject = createFlowElement(element, scope, sequenceFlows, bpmnDiElementIndex);
      eventObject.eventDefinitions = [];

      var child = element.firstChild;
      if(!!child) {
        do {
          if(child.nodeName.indexOf("EventDefinition") != -1) {
            eventObject.eventDefinitions.push({
              type : child.nodeName
            });
          }
        } while(child = child.nextSibling);
      }

      return eventObject;
    };

    function createSequenceFlow(element, scopeActivity, bpmnDiElementIndex, index) {

      var sequenceFlow = createBpmnObject(element, scopeActivity, bpmnDiElementIndex);

      if(!!sequenceFlow.sourceRef) {
        // add to the index
        if(!index[sequenceFlow.sourceRef]) {
          index[sequenceFlow.sourceRef] = [];
        }
        index[sequenceFlow.sourceRef].push(sequenceFlow);
      }

      // extract conditions:
      var conditions = element.getElementsByTagName("conditionExpression");
      if(!!conditions && conditions.length >0) {
        var condition = conditions[0];
        sequenceFlow.condition = condition.textContent;
      }

      sequenceFlow.properties = {};

      return sequenceFlow;
    }

    /** loops over all <sequenceFlow .. /> elements and builds up a map of SequenceFlows
     */
    function createSequenceFlows(element, scopeActivity, bpmnDiElementIndex) {
      element = element.firstChild;
      var index = {};

      do {

        if(element.nodeName == "sequenceFlow" || element.localName == "sequenceFlow") {
          createSequenceFlow(element, scopeActivity, bpmnDiElementIndex, index);
        }

      } while(element = element.nextSibling);

      return index;
    };

    /** transform <parallelGateway ... /> elements */
    function transformParallelGateway(element, scopeActivity, sequenceFlows, bpmnDiElementIndex) {
      var bpmnObject = createFlowElement(element, scopeActivity, sequenceFlows, bpmnDiElementIndex);

      // count incoming sequence flows
      var incomingFlows = 0;
      for (var prop in sequenceFlows) {
        var flows = sequenceFlows[prop];
        for(var i=0; i<flows.length; i++) {
          if(flows[i].targetRef == bpmnObject.id) {
            incomingFlows++;
          }
        }
      }
      // set the number of sequenceFlows to be joined in the parallel gateway
      bpmnObject.cardinality = incomingFlows;

      return bpmnObject;
    };

    /** transform <exclusiveGateway ... /> elements */
    function transformExclusiveGateway(element, scopeActivity, sequenceFlows, bpmnDiElementIndex) {
      var bpmnObject = createFlowElement(element, scopeActivity, null, bpmnDiElementIndex);
      var outgoingFlows = bpmnObject.sequenceFlows;
      var defaultFlowId = bpmnObject.default;

      // custom handling of sequence flows for exclusive GW:
      if(!!sequenceFlows && isExecutable) {
        var outgoingFlows = sequenceFlows[bpmnObject.id];
        if(!!outgoingFlows) {
          bpmnObject.sequenceFlows = outgoingFlows;
        }
        if(!!outgoingFlows) {
          if(outgoingFlows.length == 1) {
            if(!!outgoingFlows[0].condition) {
              throw "If an exclusive Gateway has a single outgoing sequence flow, the sequence flow is not allowed to have a condition.";
            }
          } else if(outgoingFlows.length > 1) {
            for (var i = 0; i < outgoingFlows.length; i++) {
              var sequenceFlow = outgoingFlows[i];

              if (!!sequenceFlow.condition) {
                if (!!defaultFlowId && defaultFlowId == sequenceFlow.id) {
                  throw "Sequence flow with id '" + sequenceFlow.id + "' is configured to be the default flow but has a condition";
                }

              } else {
                if(defaultFlowId != sequenceFlow.id) {
                  throw "Sequence flow with id '" + sequenceFlow.id + "' has no conditions but it is not configured to be the default flow.";
                }
              }
            }
          }
        }
      }
      return bpmnObject;
    };

    /** invokes all parse listeners */
    function invokeParseListeners(bpmnObject, element, scopeActivity, scopeElement) {
      for(var i=0; i<parseListeners.length; i++) {
        var parseListener = parseListeners[i];
        parseListener(bpmnObject, element, scopeActivity, scopeElement);
      }
    }

    /** transforms all activites inside a scope into ActivityDefinitions */
    function transformScope(scopeElement, scopeActivity, bpmnDiElementIndex) {

      scopeActivity.baseElements = [];

      // first, transform the sequenceflows
      var sequenceFlows = createSequenceFlows(scopeElement, scopeActivity, bpmnDiElementIndex);

      var element = scopeElement.firstChild;

      do {

        var bpmnObject = null;

        var elementType = element.nodeName;

        var taskElementTypes = ["task", "manualTask", "serviceTask", "scriptTask", "userTask", "sendTask", "recieveTask", "businessRuleTask"];
        var eventElementTypes = ["startEvent", "endEvent",  "intermediateThrowEvent", "intermediateCatchEvent", "boundaryEvent"];

        if(elementType == "exclusiveGateway") {
          bpmnObject = transformExclusiveGateway(element, scopeActivity, sequenceFlows, bpmnDiElementIndex);

        } else if(elementType == "parallelGateway") {
          bpmnObject = transformParallelGateway(element, scopeActivity, sequenceFlows, bpmnDiElementIndex);

        } else if(taskElementTypes.indexOf(elementType) != -1) {
          bpmnObject = transformTask(element, scopeActivity, sequenceFlows, bpmnDiElementIndex);

        } else if(eventElementTypes.indexOf(elementType) != -1) {
          bpmnObject = transformEvent(element, scopeActivity, sequenceFlows, bpmnDiElementIndex);

        } else if(elementType == "laneSet") {
          bpmnObject = transformLaneSet(element, scopeActivity, bpmnDiElementIndex);

        } else if(elementType == "subProcess") {
          bpmnObject = transformElementsContainer(element, scopeActivity, sequenceFlows, bpmnDiElementIndex);
        } else if(!!element && element.nodeName != "sequenceFlow") {
          bpmnObject = createBpmnObject(element, scopeActivity, bpmnDiElementIndex);
        }

        if(!!bpmnObject) {
          invokeParseListeners(bpmnObject, element, scopeActivity, scopeElement);
        }

      } while(element = element.nextSibling);
    };

    /** transforms a <process ... /> element into the corresponding Javascript Object */
    function transformProcess(processElement, bpmnDiElementIndex) {

      var bpmnObject = createFlowElement(processElement, null, null, bpmnDiElementIndex);

      if(!!bpmnObject.isExecutable) {
        isExecutable = bpmnObject.isExecutable=="true";
      } else {
        isExecutable = false;
      }

      // transform a scope
      transformScope(processElement, bpmnObject, bpmnDiElementIndex);

      generatedElements.push(bpmnObject);

      invokeParseListeners(bpmnObject, processElement);
    };

    function transformElementsContainer(containerElement, scope, sequenceFlows, bpmnDiElementIndex) {
      var containerObject = createFlowElement(containerElement, scope, sequenceFlows, bpmnDiElementIndex);

      // transform a scope
      transformScope(containerElement, containerObject, bpmnDiElementIndex);

      generatedElements.push(containerObject);

      invokeParseListeners(containerObject, containerElement);
    };

    function transformDiElementToObject(element, object) {
      var properties = {};

      properties["type"] = element.localName;
      for(var i=0; element.attributes != null && i<element.attributes.length; i++) {
        var attribute = element.attributes.item(i);
        if(attribute.nodeName != "bpmnElement") {
          properties[attribute.nodeName] = attribute.nodeValue;
        }
      }

      var childObjects = [];
      var childElement = element.firstChild;
      if(!!childElement) {
        do{
          transformDiElementToObject(childElement, childObjects);
        } while(childElement = childElement.nextSibling);
      }
      if(childObjects.length > 0) {
        properties['children'] = childObjects;
      }

      object.push(properties);
    }

    function createBpmnDiElementIndex(bpmnDiElement, bpmnDiElementIndex) {
      var bpmnElement;
      if(!!bpmnDiElement.namespaceURI && bpmnDiElement.namespaceURI == NS_BPMN_DIAGRAM_INTERCHANGE) {
        bpmnElement = bpmnDiElement.getAttribute("bpmnElement");
      }

      var element = bpmnDiElement.firstChild;
      if(!!element) {
        do {
          if(bpmnDiElement.localName == "BPMNDiagram" || bpmnDiElement.localName ==  "BPMNPlane") {
            createBpmnDiElementIndex(element, bpmnDiElementIndex);
          } else {
            var diElements = [];

            transformDiElementToObject(bpmnDiElement, diElements);

            bpmnDiElementIndex[bpmnElement] = diElements;
          }
        } while(element = element.nextSibling);
      }
    }

    /** transforms a <definitions ... /> element into a set of activity definitions */
    function transformDefinitions(definitionsElement) {

      // first, we walk the DI and index DI elements by their "bpmnElement"-id references.
      // this allows us to walk the semantic part second and for each element in the semantic-part
      // efficiently retreive the corresponding DI element
      var bpmnDiagrams = definitionsElement.getElementsByTagNameNS(NS_BPMN_DIAGRAM_INTERCHANGE, "BPMNDiagram");

      var bpmnDiElementIndex = {};
      for(var i=0; i < bpmnDiagrams.length; i++) {
        createBpmnDiElementIndex(bpmnDiagrams[i], bpmnDiElementIndex);
      }

      var participants = definitionsElement.getElementsByTagName("participant");
      if (participants.length != 0) {
        for (var index = 0; index < participants.length; index++) {
          var participant = participants[index];
          var processRef = participant.getAttribute("processRef");
          var participantId = participants[index].getAttribute("id");
          // map participant shape to process shape for resolution in transform process
          bpmnDiElementIndex[processRef] = bpmnDiElementIndex[participantId];
        }
      }

      var processes = definitionsElement.getElementsByTagName("process");

      for(var i =0; i <processes.length; i++) {
        transformProcess(processes[i], bpmnDiElementIndex);
      }

    };

    transformDefinitions(definitions[0]);

    return generatedElements;
  };

  return Transformer;
});