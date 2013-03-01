"use strict";

define(["angular"], function(angular) {

  var module = angular.module("tasklist.pages");

  var Controller = function($rootScope, $scope, $location, $routeParams, EngineApi) {
    $scope.variables = [];

    EngineApi.getTaskList().get({ id: $routeParams.id }).$then(function (result) {
      $scope.task = result.data;

      EngineApi.getProcessInstance().variables({id : $scope.task.processInstanceId}).$then(function (result) {
        var variables = result.data.variables;

        for (var index in variables) {
          var variable = variables[index];
          $scope.variables.push({key:variable.name, value : variable.value, type: variable});
        }

        console.log(variables);
      });

    });

    $scope.submitForm = function() {
      var variablesObject = {};
      for (var index in $scope.variables) {
        var variable = $scope.variables[index];
        variablesObject[variable.key] = variable.value;
      }

      EngineApi.getTaskList().complete({ id: $routeParams.id}, { variables : variablesObject }).$then(function() {
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

  Controller.$inject = ["$rootScope", "$scope", "$location", "$routeParams", "EngineApi"];

  var RouteConfig = function($routeProvider) {
    $routeProvider.when("/form/:id", {
      templateUrl: "pages/form.html",
      controller: Controller
    });
  };

  RouteConfig.$inject = [ "$routeProvider"];

  module
    .config(RouteConfig)
    .controller("FormController", Controller);

});