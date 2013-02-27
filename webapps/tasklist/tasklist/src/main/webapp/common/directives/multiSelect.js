"use strict";

define(["angular", "jquery"], function(angular, $) {

  var module = angular.module("common.directives");

  function MultiSelectController($scope) {

    $scope.selection = [];

    this.deselectAll = function() {
      var selection = $scope.selection;

      while (selection.length) {
        selection.pop();
      }
    };

    this.isSelected = function(item) {

      var selection = $scope.selection,
          idx = selection.indexOf(item);

      return idx !== -1;
    };

    this.select = function(item, exclusive) {
      if (exclusive) {
        this.deselectAll();
      }

      var selection = $scope.selection;

      if (!this.isSelected(item)) {
        selection.push(item);
      }
    };

    this.toggleSelection = function(item, exclusive) {
      if (!this.isSelected(item)) {
        this.select(item, exclusive);
      } else {
        this.deselect(item);
      }
    }

    this.deselect = function(item) {
      var selection = $scope.selection,
          idx = selection.indexOf(item);

      if (idx != -1) {
        $scope.selection.splice(idx, 1);
      }
    }
  }

  MultiSelectController.$inject = ["$scope"];

  function MultiSelectDirective() {
    return {
      restrict: "A",
      controller: "MultiSelectController",
      link: function(scope, element, attributes) {
        scope.$watch(attributes["multiSelect"], function(newValue) {
          scope.selection = newValue;
        });
      }
    };
  }

  function SelectDirective() {
    return {
      restrict: "A",
      require: "^multiSelect",
      link: function(scope, element, attributes, multiSelect) {
        var selection = scope.$eval(attributes["select"]);

        function select(e) {
          toggleSelection(e);
        };

        var enableSelection = function(e) {
          if (e.ctrlKey) {
            element.css({ cursor: "pointer" });
            element.on('click', select);
          }
        }

        var disableSelection = function(e) {
          element.off('click', select);
          element.css({ cursor: "auto" });
        }

        function toggleSelection(exclusive) {
          scope.$apply(function() {
            multiSelect.toggleSelection(selection, exclusive);
          });
        }

        $(element).hover(function() {
          $(document).on('keydown', enableSelection);
          $(document).on('keyup', disableSelection);
        }, function(e) {
          $(document).off('keydown', enableSelection);
          $(document).off('keyup', disableSelection);

          disableSelection(e);
        });
      }
    };
  };

  module
    .controller("MultiSelectController", MultiSelectController)
    .directive("multiSelect", MultiSelectDirective)
    .directive("select", SelectDirective);
});