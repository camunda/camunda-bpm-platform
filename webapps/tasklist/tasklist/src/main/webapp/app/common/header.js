"use strict";

define(["angular"], function(angular) {

  var module = angular.module("tasklist.pages");

  var ProcessDefinitionsController = function($scope, EngineApi) {
    $scope.processDefinitions = EngineApi.getProcessDefinitions().query();
  };

  ProcessDefinitionsController.$inject = ["$scope", "EngineApi"];

  var LogoutController = function($scope, $location, Authentication) {

    $scope.logout = function() {
      Authentication.logout().then(function(success) {
        $location.path("/login");
      });
    };
  };

  LogoutController.$inject = ["$scope", "$location", "Authentication"];

  module
    .controller("LogoutController", LogoutController)
    .controller("ProcessDefinitionsController", ProcessDefinitionsController);
});