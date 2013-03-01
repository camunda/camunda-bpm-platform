"use strict";

define(["angular"], function(angular) {

  var module = angular.module("tasklist.pages");

  var Controller = function($scope, $routeParams, $location, $rootScope, EngineApi) {
    var processDefinitionId = $routeParams.id;

    $scope.processDefinition = EngineApi.getProcessDefinitions().get({ id: processDefinitionId });

    $scope.variables = [];

    var form = $scope.form = {
      generic: $location.hash() == 'generic'
    };

    form.data = EngineApi.getProcessDefinitions().getStartForm({ id: processDefinitionId }).$then(function(response) {
      var data = response.resource,
                 key = data.key,
                 EMBEDDED_KEY = "embedded:";

      if (key && key.indexOf(EMBEDDED_KEY) == 0) {
        key = key.substring(EMBEDDED_KEY.length);
        form.embedded = true;
      }

      form.key = key;
      form.loaded = true;
    });

    $scope.enableGenericForm = function() {
      $location.hash('generic');
      form.generic = true;
    };

    $scope.submitForm = function() {
      var variablesObject = {};
      for (var index in $scope.variables) {
        var variable = $scope.variables[index];
        variablesObject[variable.key] = variable.value;
      }

      EngineApi.getProcessDefinitions().startInstance({ id: $routeParams.id}, { variables : variablesObject }).$then(function() {
        $rootScope.$broadcast("tasklist.reload");
        $location.hash('');
        $location.path("/overview");
      });
    };

    $scope.cancel = function() {
      $location.hash('');
      $location.path("/overview");
    };
  };

  Controller.$inject = ["$scope", "$routeParams", "$location", "$rootScope", "EngineApi"];

  var RouteConfig = function($routeProvider) {
    $routeProvider.when("/processDefinition/:id/start", {
      templateUrl: "pages/start.html",
      controller: Controller
    });
  };

  RouteConfig.$inject = [ "$routeProvider"];

  module
    .config(RouteConfig)
    .controller("StartProcessInstanceController", Controller);
});