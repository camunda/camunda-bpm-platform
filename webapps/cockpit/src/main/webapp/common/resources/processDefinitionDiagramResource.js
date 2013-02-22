angular
  .module('cockpit.resource.process.definition.diagram', ['cockpit.service.http.utils'])
  .factory('ProcessDefinitionDiagramService', function($http, HttpUtils, Uri) {
    return {
      getBpmn20Xml : function(processDefinitionId) {
        if (!processDefinitionId) {
          throw "ProcessDefinitionId is not set.";
        }

        var httpConfig = {
          method: 'GET',
          url: Uri.restUri('/process-definition/' + processDefinitionId),
          headers: { 'Content-Type': 'application/bpmn20+xml' },
          data: {}
        };
        return HttpUtils.makePromise($http(httpConfig));
      }
    }
  });