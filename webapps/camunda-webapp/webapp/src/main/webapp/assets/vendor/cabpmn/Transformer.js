/////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////

/**
 * The BPMN 2.0 transformer module
 *
 * This module provides the functionality necessary to transform
 * a BPMN 2.0 XML file into a Tree of Java Script objects that can be consumed
 * by both the Executor (process engine) and the Renderer
 * 
 * @author Daniel Meyer
 * @author Andreas Drobisch
 */
define([], function () {

  // XML namespaces
  var NS_BPMN_SEMANTIC = "http://www.omg.org/spec/BPMN/20100524/MODEL";
  var NS_BPMN_DIAGRAM_INTERCHANGE = "http://www.omg.org/spec/BPMN/20100524/DI";
  var NS_OMG_DC = "http://www.omg.org/spec/DD/20100524/DC";
  var NS_OMG_DI = "http://www.omg.org/spec/DD/20100524/DI";

  function getXmlObject(source) {
    // use the browser's DOM implemenation
    var xmlDoc;
    if (source instanceof Document) {
        xmlDoc = source;
    } else if (window.DOMParser) {
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
    this.parseListeners = [];
  }

  Transformer.prototype.transform =  function(source) {

    /** the parse listeners are callbacks that are invoked by the transformer
     * when activity definitions are created */
    var parseListeners = this.parseListeners;

    var doc = getXmlObject(source);
    var definitions = doc.getElementsByTagNameNS(NS_BPMN_SEMANTIC, "definitions");

    if(definitions.length == 0) {
      throw error("A BPMN 2.0 XML file must contain at least one definitions element");
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
        var text = element.getElementsByTagNameNS(NS_BPMN_SEMANTIC, "text")[0].firstChild.data;
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

      function getChildElementByName(element, name) {
        for (var index = 0; index < element.childNodes.length; index++) {
            if (element.childNodes[index].localName == name) {
              return element.childNodes[index];
            }
        }
      }

      var miElement = getChildElementByName(element, "multiInstanceLoopCharacteristics");
      var loop = getChildElementByName(element, "standardLoopCharacteristics");

      bpmnObject.marker = {};

      if (miElement && miElement.getAttribute("isSequential") === "true") {
        bpmnObject.marker["multiInstanceSequential"] = true;
      }else if (miElement) {
        bpmnObject.marker["multiInstanceParallel"] = true;
      }

      if (loop) {
        bpmnObject.marker["loop"] = true;
      }

      if(bpmnObject.isForCompensation == "true") {
        bpmnObject.marker["compensation"] = true;
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
      bpmnObject.properties = {};

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
                  throw error("Sequence flow with id '" + sequenceFlow.id + "' is configured to be the default flow but has a condition");
                } else {
                  // if a default flow is configured, there needs to be at least one conditional flow:
                  conditionalFlowFound = true;
                }
              }
            }

            if(!conditionalFlowFound) {
              throw error("Activity with id '"+bpmnObject.id+"' declares default flow with id '" + bpmnObject.default + "' but has no conditional flows.");
            }
          }
        }
      }

      return bpmnObject;
    }

    function transformActivity(element, scope, sequenceFlows, bpmnDiElementIndex) {
      // the ActivityDefinition to be built

      var taskObject = createFlowElement(element, scope, sequenceFlows, bpmnDiElementIndex);
      return taskObject;
    }

    function transformIoSpecification(element, scope, bpmnDiElementIndex) {
      var ioObject = createBpmnObject(element, scope, bpmnDiElementIndex);
      var inputElements = element.getElementsByTagNameNS(NS_BPMN_SEMANTIC, "dataInput");
      var outputElements = element.getElementsByTagNameNS(NS_BPMN_SEMANTIC, "dataOutput");

      var baseElements = [];

      for (var index = 0; index < inputElements.length; index++) {
        baseElements.push(createBpmnObject(inputElements[index], scope, bpmnDiElementIndex));
      }

      for (var index = 0; index < outputElements.length; index++) {
        baseElements.push(createBpmnObject(outputElements[index], scope, bpmnDiElementIndex));
      }

      ioObject["baseElements"] = baseElements;

      return ioObject;
    }

    function transformLaneSet(laneSetElement, scope, bpmnDiElementIndex) {
      if (laneSetElement.childNodes.length == 0) {
        return;
      }

      // TODO not creating a seperate bpmn object for the lane set, adding lanes to the process directly
      var element = laneSetElement.firstChild;

      do {
        var elementType = element.nodeName;
        if (elementType == "lane") {
        }
        createBpmnObject(element, scope, bpmnDiElementIndex);
      }
      while (element = element.nextSibling)
    }


    function transformEvent(element, scope, sequenceFlows, bpmnDiElementIndex) {
      // the ActivityDefinition to be built

      var eventObject = createFlowElement(element, scope, sequenceFlows, bpmnDiElementIndex);
      eventObject.eventDefinitions = [];

      var child = element.firstChild;
      if(!!child) {
        do {
          if(child.nodeName.indexOf("EventDefinition") != -1) {
            var elementType = child.nodeName;
            if (elementType.indexOf(":") != -1) {
              elementType = elementType.substr(elementType.indexOf(":") + 1, elementType.length);
            }
            eventObject.eventDefinitions.push({
              type : elementType
            });
          }
        } while(child = child.nextSibling);
      }

      return eventObject;
    }

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
      var conditions = element.getElementsByTagNameNS(NS_BPMN_SEMANTIC, "conditionExpression");
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

      if (!element) {
        // no children
        return index;
      }

      do {
        if(element.nodeName == "sequenceFlow" || element.localName == "sequenceFlow") {
          createSequenceFlow(element, scopeActivity, bpmnDiElementIndex, index);
        }
      } while(element = element.nextSibling);

      return index;
    }

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
    }

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

          if(outgoingFlows.length == 1) {

            if(!!outgoingFlows[0].condition) {
              throw error("If an exclusive Gateway has a single outgoing sequence flow, the sequence flow is not allowed to have a condition.");
            }

          } else

          if(outgoingFlows.length > 1) {
            var flowsWithoutCondition = [];

            for (var i = 0, sequenceFlow; !!(sequenceFlow = outgoingFlows[i]); i++) {

              var hasCondition = !!sequenceFlow.condition,
                  isDefaultFlow = (defaultFlowId === sequenceFlow.id);

              if (!hasCondition && !isDefaultFlow) {
                flowsWithoutCondition.push(sequenceFlow);
              }

              if (hasCondition && isDefaultFlow) {
                throw error("Sequence flow with id '" + sequenceFlow.id + "' is configured to be the default flow but has a condition.");
              }
            }

            if (!!defaultFlowId || flowsWithoutCondition.length > 1) {
              throw error("Exclusive Gateway '" + bpmnObject.id + "' has outgoing sequence flows without conditions which are not the default flow.");
            }

            if (flowsWithoutCondition.length === 1) {
              // here will not be thrown an error to align it with the java engine: 
              // in that case the java engine only log a warning!
              console.log("[Transformer]: Sequence flow with id '" + flowsWithoutCondition[0].id + "' has no conditions but it is not configured to be the default flow.");              
            }
          }
        }
      }
      return bpmnObject;
    }

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

      if (!element) {
        // no children
        return;
      }

      do {
        var bpmnObject = null;

        var elementType = element.nodeName;

        if (elementType.indexOf(":") != -1) {
          elementType = elementType.substr(elementType.indexOf(":") + 1, elementType.length);
        }

        var taskElementTypes = ["callActivity","task", "manualTask", "serviceTask", "scriptTask", "userTask", "sendTask", "recieveTask", "businessRuleTask"];
        var eventElementTypes = ["startEvent", "endEvent",  "intermediateThrowEvent", "intermediateCatchEvent", "boundaryEvent"];

        if(elementType == "exclusiveGateway") {
          bpmnObject = transformExclusiveGateway(element, scopeActivity, sequenceFlows, bpmnDiElementIndex);

        } else if(elementType == "parallelGateway") {
          bpmnObject = transformParallelGateway(element, scopeActivity, sequenceFlows, bpmnDiElementIndex);

        } else if(taskElementTypes.indexOf(elementType) != -1) {
          bpmnObject = transformActivity(element, scopeActivity, sequenceFlows, bpmnDiElementIndex);

        } else if(eventElementTypes.indexOf(elementType) != -1) {
          bpmnObject = transformEvent(element, scopeActivity, sequenceFlows, bpmnDiElementIndex);

        } else if(elementType == "laneSet") {
          bpmnObject = transformLaneSet(element, scopeActivity, bpmnDiElementIndex);

        } else if(elementType == "subProcess" || elementType =="adHocSubProcess" || elementType == "transaction") {
          bpmnObject = transformElementsContainer(element, scopeActivity, sequenceFlows, bpmnDiElementIndex);
        } else if(elementType == "ioSpecification"){
          bpmnObject = transformIoSpecification(element, scopeActivity, bpmnDiElementIndex);
        } else if(!!element && element.nodeName != "sequenceFlow" && element.nodeType == 1 /* (nodeType=1 => element nodes only) */ ) {
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

    function getMessageFlows (definitionsElement, bpmnDiElementIndex) {
      var messageFlows = [];
      var messageFlowElements = definitionsElement.getElementsByTagNameNS(NS_BPMN_SEMANTIC, "messageFlow");

      for (var i = 0, mfe; !!(mfe = messageFlowElements[i]); i++) {
        var flow = createBpmnObject(mfe, null, bpmnDiElementIndex);
        messageFlows.push(flow);
      }

      return messageFlows;
    }

    function getParticipants (definitionsElement, bpmnDiElementIndex) {
      var participants = [];
      var participantElements = definitionsElement.getElementsByTagNameNS(NS_BPMN_SEMANTIC, "participant");

      for (var index = 0; index < participantElements.length; index++) {
        var participant = createBpmnObject(participantElements[index], null, bpmnDiElementIndex);
        participants.push(participant);
      }

      return participants;
    }

    function getCategoryValues (definitionsElement) {
      var categoryValues = [];
      var categoryElements = definitionsElement.getElementsByTagNameNS(NS_BPMN_SEMANTIC, "categoryValue");

      if (categoryElements.length != 0) {
        for (var index = 0; index < categoryElements.length; index++) {
          var value = createBpmnObject(categoryElements[index], null, []);
          categoryValues.push(value);
        }
      }

      return categoryValues;
    }

    function getDataAssociations(definitionsElement, bpmnDiElementIndex) {
      var associations = [];

      var inputAssociationElements = definitionsElement.getElementsByTagNameNS(NS_BPMN_SEMANTIC, "dataInputAssociation");
      var outputAssociationElements = definitionsElement.getElementsByTagNameNS(NS_BPMN_SEMANTIC, "dataOutputAssociation");

      for (var j = 0, inputElement; !!(inputElement = inputAssociationElements[j]); j++) {
        associations.push(createBpmnObject(inputElement, null, bpmnDiElementIndex));
      }

      for (var k = 0, outputElement; !!(outputElement = outputAssociationElements[k]); k++) {
        associations.push(createBpmnObject(outputElement, null, bpmnDiElementIndex));
      }

      return associations;
    }

    function getMessages(definitionsElement, bpmnDiElementIndex) {
      var messages = [];
      var messageElements = definitionsElement.getElementsByTagNameNS(NS_BPMN_SEMANTIC, "message");

      for (var i = 0; i < messageElements.length; i++) {
        var m = createBpmnObject(messageElements[i], null, bpmnDiElementIndex);
        messages.push(m);
      }

      return messages;
    }

    /** transforms a <definitions ... /> element into a set of activity definitions */
    function transformDefinitions(definitionsElement) {

      // first, we walk the DI and index DI elements by their "bpmnElement"-id references.
      // this allows us to walk the semantic part second and for each element in the semantic-part
      // efficiently retreive the corresponding DI element
      var bpmnDiagrams = definitionsElement.getElementsByTagNameNS(NS_BPMN_DIAGRAM_INTERCHANGE, "BPMNDiagram");

      var bpmnDiElementIndex = {};
      for(var i = 0; i < bpmnDiagrams.length; i++) {
        createBpmnDiElementIndex(bpmnDiagrams[i], bpmnDiElementIndex);
      }

      var processes = definitionsElement.getElementsByTagNameNS(NS_BPMN_SEMANTIC, "process");
      var processNames = {};

      var participants = getParticipants(definitionsElement, bpmnDiElementIndex);
      var categoryValues = getCategoryValues(definitionsElement);

      generatedElements = categoryValues.concat(generatedElements.concat(participants));

      for (var j = 0, participant; !!(participant = participants[j]); j++) {

        if (!participant.processRef) {
          continue;
        }

        // map participant shape to process shape for resolution in transform process
        bpmnDiElementIndex[participant.processRef] = bpmnDiElementIndex[participant.id];

        processNames[participant.processRef] = participant.name;
      }

      for(var k = 0, process; !!(process = processes[k]); k++) {
        var name = processNames[process.getAttribute("id")];
        if (name) {
          process.setAttributeNS(NS_BPMN_SEMANTIC, "name" , processNames[process.getAttribute("id")]);
        }

        transformProcess(process, bpmnDiElementIndex);
      }

      var messageFlows = getMessageFlows(definitionsElement, bpmnDiElementIndex);
      var dataAssociations = getDataAssociations(definitionsElement, bpmnDiElementIndex);
      var messages = getMessages(definitionsElement, bpmnDiElementIndex);

      return generatedElements.concat(messageFlows, dataAssociations, messages);
    }

    return transformDefinitions(definitions[0]);
  };

  function error(message) {
    return new Error(message);
  }

  return Transformer;
});
