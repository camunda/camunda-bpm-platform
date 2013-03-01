define(["bpmn/Transformer", "bpmn/Renderer", "dojo/request", "dojo/Deferred", "dojo/query", "dojo/_base/array", "dojo/dom-class"], function (Transformer, Renderer, request, Deferred, query, array, domClass) {
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

  Bpmn.prototype.render = function (bpmnXml, options) {
    var processDefinition = new Transformer().transform(bpmnXml);
    console.log(processDefinition);

    var definitionRenderer = new Renderer(processDefinition);
    definitionRenderer.render(options);

    this.definitionRenderer = definitionRenderer;
    this.bpmnXml = bpmnXml;

    return this;
  },

  Bpmn.prototype.zoom = function (factor) {
    var transform = this.definitionRenderer.gfxGroup.getTransform();
    
    var oldFactor = 1;
    if (!!transform) {
      oldFactor = transform.xx;
    }
    
    this.definitionRenderer.gfxGroup.setTransform({xx:factor, yy:factor});
    var currentDimension = this.definitionRenderer.getSurface().getDimensions();
    this.definitionRenderer.getSurface().setDimensions(+currentDimension.width/oldFactor * factor, +currentDimension.height/oldFactor * factor);

    array.forEach(query(".bpmnElement"), function(element) {
      element.style.left = element.style.left.split("px")[0]/oldFactor * factor + "px";
      element.style.top = element.style.top.split("px")[0]/oldFactor * factor + "px";
      element.style.width = element.style.width.split("px")[0]/oldFactor * factor + "px";
      element.style.height = element.style.height.split("px")[0]/oldFactor * factor + "px";
    });

    return this;
  };

  Bpmn.prototype.annotate = function (id, innerHTML, classesArray) {
    var element = query(".bpmnElement" + "#"+id)[0];
    if (!element) {
      return;
    }

    element.innerHTML = innerHTML;

    domClass.add(element, classesArray.join(" "));

    return this;
  };

  return Bpmn;
});