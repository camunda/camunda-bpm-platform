angular
  .module('cockpit.resource.process.definition.diagram', ['cockpit.service.http.utils'])
  .factory('ProcessDefinitionDiagramService', function($http, HttpUtils, Uri) {
    return {
      getBpmn20Xml : function(processDefinitionId) {
        if (!processDefinitionId) {
          throw "ProcessDefinitionId is not set.";
        }

        return HttpUtils.makePromise(
          $http.get(Uri.restUri('/process-definition/' + processDefinitionId) + '/xml')
        );
      }
    }
  });