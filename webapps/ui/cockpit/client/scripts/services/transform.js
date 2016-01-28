'use strict';

var BpmnIO = require('bpmn-js/lib/NavigatedViewer');

  var Service = ['$q', function ($q) {
    return {
      transformBpmn20Xml: function(bpmn20Xml) {
        var deferred = $q.defer();

        BpmnIO.prototype.options = {};
        var moddle = BpmnIO.prototype.createModdle();
        moddle.fromXML(bpmn20Xml, 'bpmn:Definitions', function(err, definitions, context){
          deferred.resolve({
            definitions: definitions,
            bpmnElements: context.elementsById
          });
        });

        return deferred.promise;
      }
    };
  }];
  module.exports = Service;
