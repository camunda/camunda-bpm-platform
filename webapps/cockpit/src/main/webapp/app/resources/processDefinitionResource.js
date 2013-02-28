"use strict";

(function() {

  var module = angular.module("cockpit.resources");
  
  var Resource = function ($resource, Uri) {
    return $resource(Uri.restUri('/process-definition/:id'), {id: '@id'}, {
      'queryStatistics':  {method:'GET', isArray:true, params: { id: 'statistics' }}
    });
  };
  
  Resource.$inject = ["$resource", "Uri"];
  
  module
    .factory("ProcessDefinitionResource", Resource);

})();
  