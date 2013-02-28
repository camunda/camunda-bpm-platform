"use strict";

define(["angular"], function(angular) {

  var module = angular.module("tasklist.pages");

  var Controller = function($rootScope, $scope, $location, EngineApi, Authentication) {

    $scope.taskList = {};

    $scope.loadTasks = function (filter, search) {
      if (!Authentication.current()) {
        return;
      }

      $scope.currentFilter = filter;
      $scope.currentSearch = search;

      var queryObject = {};

      queryObject.userId = Authentication.current();

      switch (filter) {
        case "mytasks":
          queryObject.assignee = Authentication.current();
          break;
        case "colleague":
          queryObject.assignee = search;
          break;
        case "group":
          queryObject.candidateGroup = search;
          break;
      }

      $scope.taskList.tasks = EngineApi.getTasklist().query(queryObject);
    }

    $scope.$watch(function() { return $location.search(); }, function(newValue) {
      $scope.loadTasks(newValue.filter || "mytasks", newValue.search);
    });

    $scope.$on("tasklist.reload", function () {
      $scope.loadTasks($scope.currentFilter, $scope.currentSearch);
    });

    $scope.startTask = function(task) {

    };

    $scope.claimTask = function(task) {
      EngineApi.getTasklist().claim( { taskId : task.id}, { userId: Authentication.current() }).$then(function () {
        $rootScope.$broadcast("tasklist.reload");
      });
    };

    $scope.claimTasks = function (selection) {
      for (var index in selection) {
        var task = selection[index];
        $scope.claimTask(task);
      }
    };

    $scope.delegateTask = function(task) {
    };

    $scope.isSelected = function(task) {
      return $scope.taskList.selection.indexOf(task) != -1;
    };

    $scope.selectAllTasks = function() {

      $scope.deselectAllTasks();

      var selection = $scope.taskList.selection,
          tasks = $scope.taskList.tasks;

      angular.forEach(tasks, function(task) {
        selection.push(task);
      });
    };

    $scope.deselectAllTasks = function() {
      return $scope.taskList.selection = [];
    };

    $scope.taskList = {
      tasks: [],
      selection: []
    };
  };

  Controller.$inject = ["$rootScope", "$scope", "$location", "EngineApi", "Authentication"];

  var RouteConfig = function($routeProvider) {
    $routeProvider.when("/overview", {
      templateUrl: "pages/overview.html",
      controller: Controller
    });
  };

  RouteConfig.$inject = [ "$routeProvider"];

  module
    .config(RouteConfig)
    .controller("OverviewController", Controller);
});