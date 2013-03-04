"use strict";

(function() {

  var module = angular.module("cockpit.resources");
  
  var Resource = function ($http, HttpUtils, Uri) {
    return {
      getBpmn20Xml : function(processDefinitionId) {
        if (!processDefinitionId) {
          throw "ProcessDefinitionId is not set.";
        }
        
        return HttpUtils.makePromise(
            $http.get(Uri.restUri('/process-definition/' + processDefinitionId) + '/xml')
        );
      }
    };
  };
  
  Resource.$inject = ["$http", "HttpUtils", "Uri"];
  
  module
    .factory('ProcessDefinitionDiagramService', Resource);
  
})();
