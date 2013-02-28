"use strict";

define(["angular"], function(angular) {

  var module = angular.module("tasklist.pages");

  var Controller = function($scope, $location, EngineApi) {

  };

  Controller.$inject = ["$scope", "$location", "EngineApi"];

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