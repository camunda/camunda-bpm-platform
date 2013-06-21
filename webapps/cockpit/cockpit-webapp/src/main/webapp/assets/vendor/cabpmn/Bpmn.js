define([
        "bpmn/Transformer",
        "bpmn/Renderer",
        "dojo/request",
        "dojo/Deferred",
        "dojo/query",
        "dojo/_base/array",
        "dojo/dom-class",
        "dojo/dom-construct"], function (Transformer, Renderer, request, Deferred, query, array, domClass, domConstruct) {
  function Bpmn() {
  };
  
  Bpmn.prototype.renderUrl  = function (url, options) {
    var deferred = new Deferred();
    var promise = request(url);
    var self = this;

    promise.then(
      function (bpmnXml) {
        var renderer = self.render(bpmnXml, options);
        self.bpmnXml = bpmnXml;
        deferred.resolve(self);
      },
      function (error) {
        throw error;
      }
    );

    return deferred;
  };

  Bpmn.prototype.renderDiagram = function(diagram, options) {
    var definitionRenderer = new Renderer(diagram);
    definitionRenderer.render(options);

    this.definitionRenderer = definitionRenderer;
    this.processDefinitions = diagram;
    this.options = options;

    // zoom the diagram to suite the bounds specified on options if any;
    var bounds = definitionRenderer.getSurface().getDimensions(), bwidth = 1, bheight = 1;

    if (bounds) {
      bwidth = parseFloat(bounds.width),
      bheight = parseFloat(bounds.height);
    }

    var scale = Math.min(
          (options.width || bwidth) / bwidth,
          (options.height || bheight) / bheight);
    
    this.zoom(scale);

    return this;
  };
  
  Bpmn.prototype.render = function(bpmnXml, options) {
    var processDefinition = new Transformer().transform(bpmnXml);
    this.bpmnXml = bpmnXml;
    this.renderDiagram(processDefinition, options);
  };

  Bpmn.prototype.zoom = function (factor) {
    var transform = this.definitionRenderer.gfxGroup.getTransform();

    var xx = 1;
    var yy = 1;

    if (!!transform) {
      xx = transform.xx;
      yy = transform.yy;
    }

    this.definitionRenderer.gfxGroup.setTransform({xx:factor, yy:factor});
    var currentDimension = this.definitionRenderer.getSurface().getDimensions();
    this.definitionRenderer.getSurface().setDimensions(+currentDimension.width/xx * factor, +currentDimension.height/xx * factor);

    array.forEach(this.getOverlays(), function(element) {
      element.style.left = element.style.left.split("px")[0]/xx * factor + "px";
      element.style.top = element.style.top.split("px")[0]/yy * factor + "px";
      element.style.width = element.style.width.split("px")[0]/xx * factor + "px";
      element.style.height = element.style.height.split("px")[0]/yy * factor + "px";
    });

    return this;
  };

  Bpmn.prototype.getOverlays = function() {
    return query(".bpmnElement");
  };

  Bpmn.prototype.getOverlay = function(id) {
    return query("#" + id);
  };

  /**
   * Attach an event handler function for one event to the selected bpmn element.
   * @param event One event type, such as "click".
   * @param bpmnElement The bpmn element to be passed to the handler.
   * @param handler A function to execute when the event is triggered.
   */
  Bpmn.prototype.on = function(event, bpmnElement, handler) {
    // TODO: implement this method.
    // the handler should get at first parameter the event, and as second parameter
    // the passed bpmnElement.
  };
  
  Bpmn.prototype.annotation = function (id) {
    var element = this.getOverlay(id)[0];
    if (!element) {
      throw new Error("Element " + id + " does not exist.");
    }

    function addClasses(el, classes) {
      domClass.add(el, (classes || []).join(" "));
    }

    function removeClasses(el, classes) {
      domClass.remove(el, (classes || []).join(" "));
    }

    return {
      /**
       * adds child annotation div bpmn element div
       * @param innerHTML the inner html of the new annotation
       * @param classesArray classes of the new annotation
       * @returns the DOM element of the new annoation
       */
      addDiv : function (innerHTML, classesArray) {
        var newElement = domConstruct.create("div", {
          innerHTML: innerHTML
        }, element);
        addClasses(newElement, classesArray);
        return newElement;
      },
      /**
       * sets the html of the bpmn element div
       * @param html
       * @returns the annotation builder object
       */
      setHtml : function (html) {
        element.innerHTML = html;
        return this;
      },
      /**
       * adds classes to the bpmn element div
       * @param classesArray
       * @returns {*}
       */
      addClasses : function (classesArray) {
        addClasses(element, classesArray);
        return this;
      },

      removeClasses : function (classesArray) {
        removeClasses(element, classesArray);
        return this;
      }
    };
  };

//  /**
//   * @deprecated use Bpmn.prototype.annotation instead
//   */
//  Bpmn.prototype.annotate = function (id, innerHTML, classesArray) {
//    var element = this.getOverlay(id)[0];
//    if (!element) {
//      return;
//    }
//
//    element.innerHTML = innerHTML;
//
//    domClass.add(element, (classesArray || []).join(" "));
//    return element;
//  };

  Bpmn.prototype.clearAnnotations = function (id, classesArray) {
    var element = this.getOverlay(id)[0];
    if (!element) {
      return;
    }

    element.innerHTML = "";

    domClass.remove(element, classesArray.join(" "));
  };

  Bpmn.prototype.clear = function () {
    this.definitionRenderer.gfxGroup.destroy();
    domConstruct.empty(query("#"+this.options.diagramElement)[0]);
  };

  return Bpmn;
});
