
define(["angular", "jquery"], function(angular, $) {

  var module = angular.module("tasklist.directives");

  /**
   * The main controller for variables.
   *
   * @param $scope {Scope}
   */
  var TaskVariablesController = function TaskVariablesController($scope) {

    this.addVariable = function(variable) {

      var variables = this.variables,
          idx = variables.indexOf(variable);

      if (idx == -1) {
        console.log("add variable", variable);
        variables.push(variable);
      }
    };

    this.removeVariable = function(variable) {
      var variables = this.variables,
          idx = variables.indexOf(variable);

      if (idx != -1) {
        console.log("remove variable", variable);
        variables.splice(idx, 1);
      }
    };
  };

  TaskVariablesController.$inject = ["$scope"];

  var TaskVariablesDirective = function TaskVariablesDirective() {
    return {
      restrict: "EA",
      controller: "TaskVariablesController",
      link: function(scope, element, attributes, controller) {

        scope.$watch(attributes["taskVariables"], function(newValue) {
          controller.variables = newValue;
        });

        scope.removeVariable = function(variable) {
          controller.removeVariable(variable);
        };

        scope.addVariable = function() {
          controller.addVariable({ key : "", value: "", type: "string" });
        };
      }
    };
  };

  var FormFieldDirective = function FormFieldDirective() {
    return {
      scope: true,
      require: "^taskVariables",
      replace: true,
      templateUrl: 'directives/form-field.html',
      link: function(scope, element, attributes, taskVariables) {

        var type = attributes["type"],
            key = attributes["key"],
            variable = scope.$eval(attributes["variable"]);

        if (variable) {
          taskVariables.addVariable(variable);
        } else {
          if (!key || !type) {
            throw new Error("key or type not defined for form field");
          }

          variable = scope.variable = { type: type, key: key }
        }

        scope.typeSwitch = attributes["typeSwitch"];

        taskVariables.addVariable(variable);

        scope.setType = function(type) {
          variable.type = type;
        };
      }
    };
  };

  module
    .controller("TaskVariablesController", TaskVariablesController)
    .directive("taskVariables", TaskVariablesDirective)
    .directive("formField", FormFieldDirective);
});