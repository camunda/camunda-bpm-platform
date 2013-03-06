
define(["angular", "jquery"], function(angular, $) {

  var module = angular.module("tasklist.directives");

  /**
   * The main controller for variables.
   *
   * @param $scope {Scope}
   */
  var TaskVariablesController = function TaskVariablesController($scope) {

    this.getVariable = function(key) {
      var variables = this.variables;

      for (var i = 0, variable; !!(variable = variables[i]); i++) {
        if (variable.key == key) {
          return variable;
        }
      }

      return null;
    };

    this.addVariable = function(variable) {

      var variables = this.variables,
          idx = variables.indexOf(variable);

      if (idx == -1) {
        variables.push(variable);
      }
    };

    this.removeVariable = function(variable) {
      var variables = this.variables,
          idx = variables.indexOf(variable);

      if (idx != -1) {
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
            readOnly = (attributes["readonly"] == "readonly" || attributes["readonly"] === true),
            variable = scope.$eval(attributes["variable"]);

        if (variable) {
          taskVariables.addVariable(variable);
        } else {
          if (!key || !type) {
            throw new Error("key or type not defined for form field");
          }

          variable = taskVariables.getVariable(key);

          if (!variable) {
            variable = { type: type, key: key };
            taskVariables.addVariable(variable);
          }
        }

        // set readonly state
        variable.readOnly = readOnly;

        scope.variable = variable;

        scope.typeSwitch = attributes["typeSwitch"];

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