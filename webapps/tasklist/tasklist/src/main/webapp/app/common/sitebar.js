"use strict";

define(["angular"], function(angular) {

  var module = angular.module("tasklist.pages");

  var Controller = function($scope, $location, EngineApi, Authentication) {
    var currentUser = Authentication.current();

    if (!currentUser) {
      return;
    }

    var tasks;

    $scope.loadGroupInfo = function () {
      tasks = $scope.tasks = {
        mytasks: EngineApi.getTaskCount().get({ "assignee" : currentUser }),
        unassigned: EngineApi.getTaskCount().get({ "assignableTo" : currentUser })
      };

      $scope.groupInfo = EngineApi.getGroups(currentUser);
    };

    $scope.isActive = function(filter, search) {
      var params = $location.search();
      return (params.filter || "mytasks") == filter && params.search == search;
    };

    $scope.$on("tasklist.reload", function () {
      $scope.loadGroupInfo();
    });

    $scope.loadGroupInfo();
  };

  Controller.$inject = ["$scope", "$location", "EngineApi", "Authentication"];

  var RouteConfig = function($routeProvider) {
    $routeProvider.when("/overview", {
      templateUrl: "pages/overview.html",
      controller: Controller
    });
  };

  RouteConfig.$inject = [ "$routeProvider" ];

  module
    .config(RouteConfig)
    .controller("SitebarController", Controller);
});