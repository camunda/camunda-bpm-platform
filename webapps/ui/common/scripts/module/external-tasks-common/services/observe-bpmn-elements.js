'use strict';

module.exports = function() {
  return function($scope, instance) {
    var scopedProcessData = instance.processData.newChild($scope);

    scopedProcessData.observe('bpmnElements', function(bpmnElements) {
      instance.bpmnElements = bpmnElements;
    });
  };
};
