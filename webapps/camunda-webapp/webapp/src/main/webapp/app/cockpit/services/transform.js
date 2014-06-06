/* global ngDefine: false */
ngDefine('cockpit.services', [ 'bpmn/Transformer' ], function(module, Transformer) {
  'use strict';

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
