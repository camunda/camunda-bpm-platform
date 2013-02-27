"use strict";

define(["angular"], function(angular) {

  var module = angular.module("tasklist.pages");

  var Controller = function($scope, $location) {

    var allTasks = {
      "mytasks": [
        { id: 1, name: "ASD", process: "asdf", created: "sadf", due: "asdsd"}],
      "unassigned": [
        { id: 1, name: "ASD", process: "asdf", created: "sadf", due: "asdsd"},
        { id: 1, name: "erer", process: "tzz", created: "sadf", due: "asdsd"},
        { id: 1, name: "ASD", process: "werw", created: "sadf", due: "asdsd"}],
      "colleague-klaus": [],
      "group-my-group": [
        { id: 1, name: "asd", process: "werwe", created: "sadf", due: "asdsd"},
        { id: 1, name: "g", process: "asdwerewrf", created: "sadf", due: "asdsd"}],
      "group-other-group": []
    };

    function loadTasks(filter, search) {
      $scope.taskList.tasks = allTasks[filter + (search ? "-" + search : "")];
      $scope.taskList.view = { filter: filter, search: search };
      $scope.taskList.selection = {};
    }

    $scope.$watch(function() { return $location.search(); }, function(newValue) {
      loadTasks(newValue.filter || "mytasks", newValue.search);
    });

    $scope.startTask = function(task) {

    };

    $scope.claimTask = function(task) {

    };

    $scope.delegateTask = function(task) {

    };

    $scope.taskList = {
      tasks: [],
      selection: []
    };
  };

  Controller.$inject = ["$scope", "$location"];

  var RouteConfig = function($routeProvider) {
    $routeProvider.when("/overview", {
      templateUrl: "pages/overview.html",
      controller: Controller
    });
  };

  RouteConfig.$inject = [ "$routeProvider"];

  function MultiSelectController($scope) {

    $scope.selection = [];

    this.select = function(item) {
      var idx = $scope.selection.indexOf(item);
      if (idx == -1) {
        $scope.selection.push(item);
      }
    }

    this.toggleSelection = function(item) {
      var idx = $scope.selection.indexOf(item);
      if (idx == -1) {
        this.select(item);
      } else {
        this.deselect(item);
      }
    }

    this.deselect = function(item) {
      var idx = $scope.selection.indexOf(item);
      if (idx != -1) {
        $scope.selection.splice(idx, 1);
      }
    }
  }

  module.directive("multiSelect", function() {
    return {
      restrict: "A",
      controller: "^MultiSelectController",
      link: function(scope, element, attributes, multiSelectController) {
        var selection = scope.$eval(attributes["multiSelection"]);
        $scope.selection = selection;
      }
    };
  });

  module.directive("select", function() {
    return {
      restrict: "A",
      require: [ "MultiSelectController" ],
      link: function(scope, element, attributes) {
        var element = scope.$eval(attributes["select"]);

      }
    };
  });

  module
    .config(RouteConfig)
    .controller("OverviewController", Controller)
    .controller("MultiSelectController", MultiSelectController);
});