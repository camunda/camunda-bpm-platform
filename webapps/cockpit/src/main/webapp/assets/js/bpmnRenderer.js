var BpmnRenderer = (function() {
  
  var paper = null;
  
  /**
   * Renders the given BPMN 2.0 xmlString inside
   * the given container to a bpmn diagram.
   */
  function render(xmlString, container) {
    paper = bpmnDirect(xmlString, container);
  }
  
  function getShapeBounds(elementId) {
    return paper.getById(elementId).getBBox();
  }
  
  return {
    render: render,
    getShapeBounds: getShapeBounds
  };
  
  
}());
