"use strict";

define(["angular"], function(angular) {

  var module = angular.module("tasklist.pages");

  var Controller = function($scope, $location, Errors, Authentication) {

    if (Authentication.current()) {
      $location.path("/overview");
    }

    $scope.login = function () {
      Authentication.login($scope.username, $scope.password).then(function(success) {
        Errors.clear("Unauthorized");
        Errors.clear("Login Failed");

        if (success) {
          $location.path("/overview");
        } else {
          Errors.add({ status: "Login Failed", message: "Username / password are incorrect" });
        }
      });
    }
  };

  Controller.$inject = ["$scope", "$location", "Errors", "Authentication"];

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