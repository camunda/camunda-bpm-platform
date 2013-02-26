"use strict";

define(["angular"], function(angular) {

  var module = angular.module("tasklist.pages");

  var Controller = function($scope) {
    $scope.foo = "KLAUS";
  };

  Controller.$inject = ["$scope"];

  var RouteConfig = function($routeProvider) {
    $routeProvider.when("/overview", {
      templateUrl: "pages/overview.html",
      controller: Controller
    });
  };

  RouteConfig.$inject = [ "$routeProvider" ];
  
  module
    .config(RouteConfig)
    .controller("OverviewController", Controller);
});