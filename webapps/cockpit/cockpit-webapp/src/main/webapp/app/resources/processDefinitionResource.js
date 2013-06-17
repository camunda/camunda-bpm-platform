'use strict';

ngDefine('cockpit.resources.process.definition', ['module:camunda.common.services:camunda-common/services/uri'], function(module) {
  
  var ProcessDefinitionResource = function ($resource, Uri) {
    return $resource(Uri.appUri('engine://process-definition/:id/:action'), { id: '@id' }, {
      queryStatistics: { method: 'GET', isArray: true, params: { id: 'statistics' }},
      queryActivityStatistics: { method: 'GET', isArray: true, params: { action: 'statistics' }},
      getBpmn20Xml: { method: 'GET', params: { action: 'xml' }},
    });
  };

  module
    .factory('ProcessDefinitionResource', ProcessDefinitionResource);

});
