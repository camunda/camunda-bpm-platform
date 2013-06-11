"use strict";

define(["angular"], function(angular, BpmnRender) {

  var module = angular.module("cockpit.pages");

  var Controller = function($scope, $routeParams, $location, Errors, ProcessDefinitionResource, ProcessInstanceResource, Views) {

    function failNoProcessDefinition() {
      $location.path('/dashboard').search({}).replace();

      Errors.add({ status : "Error" , message :  "No process definition id was provided. Auto-redirecting to main site." });
    }

    // redirect when no processDefinitionId is set
    if (!$routeParams.processDefinitionId) {
      failNoProcessDefinition();
      return;
    }

    $scope.processInstanceTable = Views.getProvider({ component: 'cockpit.process.instances'});

    $scope.processDefinitionId = $routeParams.processDefinitionId;

    ProcessDefinitionResource
      .get({ id : $scope.processDefinitionId })
        .$then(function(result) {
          $scope.processDefinition = result.resource;

          ProcessInstanceResource.count({ processDefinitionKey : $scope.processDefinition.key }).$then(function(result) {
            $scope.processDefinitionTotalCount = result.data;
          });
        }, function(err) {
          if (err.status === 400) {
            failNoProcessDefinition();
          }
        });

    ProcessInstanceResource.count({ processDefinitionId : $scope.processDefinitionId }).$then(function(result) {
      $scope.processDefinitionLatestVersionCount = result.data;
    });
  };

  Controller.$inject = [ '$scope', '$routeParams', '$location', 'Errors', 'ProcessDefinitionResource', 'ProcessInstanceResource', 'Views' ];

  var RouteConfig = function ($routeProvider) {
    $routeProvider.when('/process-definition/:processDefinitionId', {
      templateUrl: 'pages/process-definition.html',
      controller: Controller,
      reloadOnSearch: false
    });
  };

  RouteConfig.$inject = ['$routeProvider'];

  module
    .config(RouteConfig);

});
