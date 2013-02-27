"use strict";

define(["angular"], function(angular) {

  var module = angular.module("tasklist.pages");

  var Controller = function($scope, $location, Authentication) {
    $scope.login = function () {
      Authentication.login($scope.username, $scope.password).success(function () {
        $location.path("/overview");
      });
    }
  };

  Controller.$inject = ["$scope", "$location", "Authentication"];

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