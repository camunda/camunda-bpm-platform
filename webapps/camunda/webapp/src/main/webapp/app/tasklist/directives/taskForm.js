ngDefine('tasklist.directives', [
  'angular',
  'jquery'
], function(module, angular, $) {

  /**
   * The main controller for variables.
   *
   * @param $scope {Scope}
   */
  var TaskVariablesController = function TaskVariablesController($scope) {

    this.getVariable = function(name) {
      var variables = this.variables;

      for (var i = 0, variable; !!(variable = variables[i]); i++) {
        if (variable.name == name) {
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
          controller.addVariable({ name : "", value: "", type: "string" });
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
            name = attributes["name"],
            readOnly = (attributes["readonly"] == "readonly" || attributes["readonly"] === true),
            variable = scope.$eval(attributes["variable"]);

        if (variable) {
          taskVariables.addVariable(variable);
        } else {
          if (!name || !type) {
            throw new Error("name or type not defined for form field");
          }

          variable = taskVariables.getVariable(name);

          if (!variable) {
            variable = { type: type, name: name };
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