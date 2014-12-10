/* global define: false */
define([ 'bpmn/Transformer' ], function(Transformer) {
  'use strict';

  var Service = function () {
    return {
      transformBpmn20Xml: function(bpmn20Xml) {
        return new Transformer().transform(bpmn20Xml);
      }
    };
  };
  return Service;
});
