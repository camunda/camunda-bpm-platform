"use strict";

define(["angular"], function(angular) {

  var module = angular.module("tasklist.pages");

  var Controller = function($rootScope, $scope, $location, EngineApi, Authentication) {

    $scope.taskList = {};

    $scope.groupInfo = EngineApi.getGroups(Authentication.current());

    $scope.loadTasks = function(filter, search) {
      if (!Authentication.current()) {
        return;
      }

      $scope.taskList.view = { filter: filter, search: search };

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

      $scope.taskList.tasks = EngineApi.getTaskList().query(queryObject);
    };

    $scope.$watch(function() { return $location.search(); }, function(newValue) {
      $scope.loadTasks(newValue.filter || "mytasks", newValue.search);
    });

    $scope.startTask = function(task) {
      $location.path("/form/" + task.id);
    };

    $scope.claimTask = function(task) {

      EngineApi.getTaskList().claim( { id : task.id}, { userId: Authentication.current() }).$then(function () {
        var tasks = $scope.taskList.tasks;
        var view = $scope.taskList.view;

        if (view.filter == "mytasks") {
          $scope.addTask(task);
        } else {
          $scope.removeTask(task);
        }

        $rootScope.$broadcast("tasklist.reload");
      });
    };


    $scope.unclaimTask = function(task) {
      EngineApi.getTaskList().unclaim( { id : task.id}, { userId: Authentication.current() }).$then(function () {
        $scope.removeTask(task);
        $rootScope.$broadcast("tasklist.reload");
      });
    };

    $scope.addTask = function (task) {
      $scope.taskList.tasks.push(task);
    };

    $scope.removeTask = function (task) {
      var tasks = $scope.taskList.tasks;

      var idx = tasks.indexOf(task);
      if (idx != -1) {
        tasks.splice(idx, 1);
      }
    };

    $scope.claimTasks = function (selection) {
      for (var index in selection) {
        var task = selection[index];
        $scope.claimTask(task);
      }
    };

    $scope.delegateTask = function(task, user) {
      EngineApi.getTaskList().delegate( { id : task.id}, { userId: user.id}).$then(function () {
        $scope.removeTask(task);
        $rootScope.$broadcast("tasklist.reload");
      });
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