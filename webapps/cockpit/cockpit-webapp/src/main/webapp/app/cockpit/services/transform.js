'use strict';

ngDefine('cockpit.services', [ 'bpmn/Transformer' ], function(module, Transformer) {
  
  var Service = function () {
    return {
      transformBpmn20Xml: function(bpmn20Xml) {
        return new Transformer().transform(bpmn20Xml);
      }
    };
  };

  module
    .factory('Transform', Service);
  
  return module;

});
