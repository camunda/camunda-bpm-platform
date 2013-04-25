"use strict";

(function() {

  var module = angular.module("cockpit.resources");
  
  var ProcessDefinitionResource = function ($resource, Uri) {
    return $resource(Uri.restUri('/process-definition/:id'), {id: '@id'}, {
      'queryStatistics':  {method:'GET', isArray:true, params: { id: 'statistics' }}
    });
  };
  
  ProcessDefinitionResource.$inject = ["$resource", "Uri"];
  
  module
  .factory("ProcessDefinitionResource", ProcessDefinitionResource);
  
  var ProcessDefinitionActivityStatisticsResource = function ($resource, Uri) {
    return $resource(Uri.restUri('/process-definition/:id/statistics'), {id: '@id'}, {
      'queryStatistics':  {method:'GET', isArray:true, params: { id: 'statistics' }}
    });
  };
  
  ProcessDefinitionActivityStatisticsResource.$inject = ["$resource", "Uri"];
  
  module
  .factory("ProcessDefinitionActivityStatisticsResource", ProcessDefinitionActivityStatisticsResource);

})();
  