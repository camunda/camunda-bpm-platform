"use strict";

define(["angular"], function(angular) {

  var module = angular.module("tasklist.pages");

  var Controller = function($scope, $location, EngineApi, Authentication) {

    function setActive(filter) {
      $scope.tasks.active = filter;
    }

    /*$scope.tasks = {
      unassigned: { count: 10 },
      assigned: { count: 10 },
      groups: [
        { id: "my-group", name: 'my group', count: 5 },
        { id: "other-group", name: 'other group', count: 10}
      ],
      colleagues: [
        { id: "klaus", name: "klaus", count: 10 }
      ]
    };*/

    $scope.isActive = function(filter, search) {
      var params = $location.search();
      return (params.filter || "mytasks") == filter && params.search == search;
    };

    $scope.groups = EngineApi.getGroups("demo"); //Authentication.current().user);

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