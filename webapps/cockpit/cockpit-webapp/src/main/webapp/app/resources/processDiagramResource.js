'use strict';

ngDefine('cockpit.resources.process.diagram', ['module:camunda.common.services:camunda-common/services/httpUtils'], function(module) {
  
  var Resource = function ($http, HttpUtils, Uri) {
    return {
      getBpmn20Xml : function(processDefinitionId) {
        if (!processDefinitionId) {
          throw "ProcessDefinitionId is not set.";
        }

        return HttpUtils.makePromise(
          $http.get(Uri.appUri('engine://process-definition/' + processDefinitionId) + '/xml')
        );
      }
    };
  };

  module
    .factory("ProcessDiagramService", Resource);

});
