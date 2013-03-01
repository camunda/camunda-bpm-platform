define(["bpmn/Transformer", "bpmn/Renderer", "dojo/request", "dojo/Deferred", "dojo/query", "dojo/_base/array", "dojo/dom-class", "dojo/dom-construct"], function (Transformer, Renderer, request, Deferred, query, array, domClass, domConstruct) {
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
    this.options = options;

    return this;
  },

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

      array.forEach(query(".bpmnElement"), function(element) {
        element.style.left = element.style.left.split("px")[0]/xx * factor + "px";
        element.style.top = element.style.top.split("px")[0]/yy * factor + "px";
        element.style.width = element.style.width.split("px")[0]/xx * factor + "px";
        element.style.height = element.style.height.split("px")[0]/yy * factor + "px";
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
  };

  Bpmn.prototype.clear = function (id, innerHTML, classesArray) {
    this.definitionRenderer.gfxGroup.destroy();
    domConstruct.empty(query("#"+this.options.diagramElement)[0]);
  };

  return Bpmn;
});