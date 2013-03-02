"use strict";

define(["angular"], function(angular) {

  var module = angular.module("tasklist.pages");

  var Controller = function($rootScope, $scope, $location, Notifications, Authentication) {

    if (Authentication.current()) {
      $location.path("/overview");
    }

    $scope.login = function () {
      Authentication.login($scope.username, $scope.password).then(function(success) {
        Notifications.clearAll();
        
        if (success) {
          $rootScope.$broadcast("tasklist.reload");
          Notifications.add({ type: "success", status: "Login", message: "Login successful", duration: 10000 });

          $location.path("/overview");
        } else {
          Notifications.addError({ status: "Login Failed", message: "Username / password are incorrect" });
        }
      });
    }
  };

  Controller.$inject = ["$rootScope", "$scope", "$location", "Notifications", "Authentication"];

  var RouteConfig = function($routeProvider) {
    $routeProvider.when("/login", {
      templateUrl: "pages/login.html",
      controller: Controller
    });
  };

  RouteConfig.$inject = [ "$routeProvider"];

  module
    .config(RouteConfig)
    .controller("LoginController", Controller);

});