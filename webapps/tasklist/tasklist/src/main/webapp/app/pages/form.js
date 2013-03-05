"use strict";

define(["angular"], function(angular) {

  var module = angular.module("tasklist.pages");

  var Controller = function($rootScope, $scope, $location, $routeParams, EngineApi) {

    function parseFormData(data, form) {
      var key = data.formKey,
        applicationContextPath = data.applicationContextPath,
        EMBEDDED_KEY = "embedded:",
        APP_KEY = "app:";

      // structure may be [embedded:][app:]formKey

      if (!key) {
        return;
      }

      if (key.indexOf(EMBEDDED_KEY) == 0) {
        key = key.substring(EMBEDDED_KEY.length);
        form.embedded = true;
      }

      if (key.indexOf(APP_KEY) == 0) {
        if (applicationContextPath) {
          key = applicationContextPath + "/" + key.substring(APP_KEY.length);
        }
      }

      form.key = key;
    }

    $scope.variables = [];

    var form = $scope.form = {
      generic: $location.hash() == 'generic'
    };

    EngineApi.getTaskList().get({ id: $routeParams.id }).$then(function (result) {
      var task = $scope.task = result.resource;

      EngineApi.getProcessInstance().variables({ id : task.processInstanceId }).$then(function (result) {
        var variables = result.data.variables;

        for (var index in variables) {
          var variable = variables[index];
          $scope.variables.push({ key: variable.name, value: variable.value, type: variable.type.toLowerCase() });
        }
      });
    });

    form.data = EngineApi.getTaskList().getForm({ id: $routeParams.id }).$then(function(response) {
      var data = response.resource;

      parseFormData(data, form);

      if (!form.embedded) {
        var externalUrl = encodeURI(form.key + "?taskId=" + $scope.task.id + "&callbackUrl=" + $location.absUrl() + "/complete");

        window.location.href = externalUrl;
      }

      form.loaded = true;
    });

    $scope.enableGenericForm = function() {
      $location.hash('generic');
      form.generic = true;
    };

    $scope.cancel = function() {
      $location.hash('');
      $location.path("/overview");
    };

    $scope.submitForm = function() {
      var variablesObject = {};
      for (var index in $scope.variables) {
        var variable = $scope.variables[index];
        variablesObject[variable.key] = variable.value;
      }

      EngineApi.getTaskList().complete({ id: $routeParams.id }, { variables : variablesObject }).$then(function() {
        $rootScope.$broadcast("tasklist.reload");
        $location.hash('');
        $location.path("/overview");
      });
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