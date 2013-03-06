"use strict";

define(["angular"], function(angular) {

  var module = angular.module("tasklist.pages");

  var ProcessDefinitionsController = function($scope, EngineApi) {
    $scope.processDefinitions = EngineApi.getProcessDefinitions().query();
  };

  ProcessDefinitionsController.$inject = ["$scope", "EngineApi"];

  var LogoutController = function($scope, $location, Notifications, Authentication) {

    $scope.logout = function() {
      Authentication.logout().then(function(success) {
        Notifications.clearAll();
        Notifications.addMessage({ status: "Logout", message: "You have been logged out" });
        $location.path("/login");
      });
    };
  };

  LogoutController.$inject = ["$scope", "$location", "Notifications", "Authentication"];

  module
    .controller("LogoutController", LogoutController)
    .controller("ProcessDefinitionsController", ProcessDefinitionsController);
});