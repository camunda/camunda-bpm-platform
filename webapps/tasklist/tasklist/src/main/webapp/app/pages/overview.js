"use strict";

define(["angular", "bpmn/Bpmn"], function(angular, Bpmn) {

  var module = angular.module("tasklist.pages");

  var Controller = function($rootScope, $scope, $location, debounce, EngineApi, Authentication) {

    var fireTaskListChanged = debounce(function() {
      $rootScope.$broadcast("tasklist.reload");
    }, 300);

    var reloadTasks = debounce(function() {
      var view = $scope.taskList.view;

      $scope.loadTasks(view);
    }, 300);

    $scope.taskList = {
      sort: {
        by: "created",
        order: "desc"
      },
      selection: []
    };

    $scope.$on("sortChanged", function() {
      reloadTasks();
    });

    $scope.loadTasks = function(view) {
      var filter = view.filter,
          search = view.search;

      if (!Authentication.current()) {
        return;
      }

      $scope.taskList.view = { filter: filter, search: search };

      var queryObject = {},
          user = Authentication.current(),
          sort = $scope.taskList.sort;

      queryObject.userId = user;

      switch (filter) {
        case "mytasks":
          queryObject.assignee = user;
          break;
        case "unassigned":
          queryObject.assignableTo = user;
          break;
        case "colleague":
          queryObject.assignee = search;
          break;
        case "group":
          queryObject.candidateGroup = search;
          break;
      }

      queryObject.sortBy = sort.by;
      queryObject.sortOrder = sort.order;

      EngineApi.getTaskList().query(queryObject).$then(function(response) {
        $scope.taskList.tasks = response.resource;
      });
    };

    $scope.$watch(function() { return $location.search(); }, function(newValue) {
      var view = angular.extend({ filter: "mytasks" }, newValue);
      $scope.loadTasks(view);
    });

    $scope.startTask = function(task) {
      $location.path("/form/" + task.id);
    };

    $scope.claimTask = function(task) {

      EngineApi.getTaskList().claim( { id : task.id}, { userId: Authentication.current() }).$then(function () {
        var tasks = $scope.taskList.tasks,
            view = $scope.taskList.view;

        if (view.filter == "mytasks") {
          $scope.addTask(task);
        } else {
          $scope.removeTask(task);
        }

        fireTaskListChanged();
      });
    };

    $scope.unclaimTask = function(task) {
      EngineApi.getTaskList().unclaim({ id : task.id }, { userId: Authentication.current() }).$then(function () {
        $scope.removeTask(task);

        fireTaskListChanged();
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

    $scope.delegateTask = function(task, user) {
      EngineApi.getTaskList().delegate( { id : task.id}, { userId: user.id }).$then(function () {
        $scope.removeTask(task);

        fireTaskListChanged();
      });
    };

    $scope.claimTasks = function (selection) {
      for (var index in selection) {
        var task = selection[index];
        $scope.claimTask(task);
      }
    };

    $scope.isSelected = function(task) {
      return $scope.taskList.selection.indexOf(task) != -1;
    };

    $scope.select = function (task) {
      $scope.taskList.selection = [];
      $scope.taskList.selection.push(task);
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

    $scope.showDiagram = function (task, index) {
      EngineApi.getProcessDefinitions().xml({id : task.processDefinitionId}).$then( function (result) {
        var bpmnXml = result.data.bpmn20Xml;

        if ($scope.bpmn) {
          $scope.bpmn.clear();
          $scope.bpmn = null;
        }

        $scope.bpmn = new Bpmn().render(bpmnXml, {
          diagramElement : "diagram"
        });

        $scope.bpmn.annotate(task.taskDefinitionKey, "", [ "bpmnHighlight" ]);
        $scope.select(task);
      });
    };
  };

  Controller.$inject = ["$rootScope", "$scope", "$location", "debounce", "EngineApi", "Authentication"];

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