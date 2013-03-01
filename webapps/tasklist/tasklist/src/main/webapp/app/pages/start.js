"use strict";

define(["angular"], function(angular) {

  var module = angular.module("tasklist.pages");

  var Controller = function($scope, $routeParams, $location, $rootScope, EngineApi) {
    var processDefinitionId = $routeParams.id;

    $scope.processDefinition = EngineApi.getProcessDefinitions().get({ id: processDefinitionId });

    $scope.variables = [];
    $scope.generic = $location.search().generic;

    $scope.startForm = {};

    $scope.startForm.form = EngineApi.getProcessDefinitions().getStartForm({ id: processDefinitionId }).$then(function() {
      $scope.startForm.loaded = true;
    });

    $scope.enableGenericForm = function() {
      $location.search("generic");
      $scope.generic = true;
    };

    $scope.submitForm = function() {
      var variablesObject = {};
      for (var index in $scope.variables) {
        var variable = $scope.variables[index];
        variablesObject[variable.key] = variable.value;
      }

      EngineApi.getProcessDefinitions().startInstance({ id: $routeParams.id}, { variables : variablesObject }).$then(function() {
        $rootScope.$broadcast("tasklist.reload");
        $location.path("/overview");
      });
    };

    $scope.addVariable = function() {
      $scope.variables.push({ key : "key", value: "value" });
    };

    $scope.removeVariable = function (index) {
      $scope.variables.splice(index, 1);
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