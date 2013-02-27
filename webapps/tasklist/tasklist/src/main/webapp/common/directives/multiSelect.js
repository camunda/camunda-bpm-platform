"use strict";

define(["angular", "jquery"], function(angular, $) {

  var module = angular.module("common.directives");

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

  MultiSelectController.$inject = ["$scope"];

  function MultiSelectDirective() {
    return {
      restrict: "A",
      controller: "^MultiSelectController",
      link: function(scope, element, attributes) {
        var selection = scope.$eval(attributes["multiSelection"]);
        scope.selection = selection;
      }
    };
  }

  function SelectDirective() {
    return {
      restrict: "A",
      require: [ "MultiSelectController" ],
      link: function(scope, element, attributes) {
        var element = scope.$eval(attributes["select"]);

        $(element).hover(function() {
          element.css({cursor: "pointer"})
        }, function() {
          element.css({cursor: "auto" });
        });
      }
    };
  };


  module
    .directive("multiSelect", MultiSelectDirective)
    .directive("select", SelectDirective)
    .controller("MultiSelectController", MultiSelectController);
});