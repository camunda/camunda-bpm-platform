"use strict";

define(["angular"], function(angular, BpmnRender) {

  var module = angular.module("cockpit.pages");
  
  var Controller = function($scope, $routeParams, $location, Errors, ProcessDefinitionResource, ProcessInstanceResource) {
    // redirect when no processDefinitionId is set
    if (!$routeParams.processDefinitionId) {
      $location.path('/dashboard').replace();
      Errors.add({ "status" : "Error" , "config" :  "No process definition id was provided. Auto-redirecting to main site." });
    }
    
    $scope.processDefinitionId = $routeParams.processDefinitionId;
    
    ProcessDefinitionResource.get({ id : $scope.processDefinitionId }, function(result) {
      $scope.processDefinition = result;
      
      ProcessInstanceResource.count({ processDefinitionKey : $scope.processDefinition.key }).$then(function(result) {
        $scope.processDefinitionTotalCount = result.data;
      });
    });
    
    ProcessInstanceResource.count({ processDefinitionId : $scope.processDefinitionId }).$then(function(result) {
      $scope.processDefinitionLatestVersionCount = result.data;
    });
  };
  
  Controller.$inject = ["$scope", "$routeParams", "$location", "Errors", "ProcessDefinitionResource", "ProcessInstanceResource"];
  
  var RouteConfig = function ($routeProvider) {
    $routeProvider.when('/process-definition/:processDefinitionId', {
      templateUrl:'pages/process-definition.html',
      controller: 'ProcessDefinitionCtrl'
    });
  };

  RouteConfig.$inject = ["$routeProvider"];
  
  module
    .config(RouteConfig)
    .controller("ProcessDefinitionCtrl", Controller);
  
});
