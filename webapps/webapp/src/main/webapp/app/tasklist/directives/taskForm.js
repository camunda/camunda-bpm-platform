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

  TaskVariablesController.$inject = ['$scope'];

  var TaskVariablesDirective = function TaskVariablesDirective() {
    return {
      restrict: 'EA',
      controller: 'TaskVariablesController',
      link: function(scope, element, attributes, controller) {

        scope.$watch(attributes['taskVariables'], function(newValue) {
          controller.variables = newValue;
        });
        
        controller.variables = scope.$eval(attributes['taskVariables']);

        scope.removeVariable = function(variable) {
          controller.removeVariable(variable);
        };

        scope.addVariable = function() {
          controller.addVariable({ name : '', value: '', type: 'string' });
        };
      }
    };
  };

  var FormFieldDirective = [
       '$http', '$templateCache', '$compile', '$controller', '$animator',
       function($http, $templateCache, $compile, $controller) {

    return {
      scope: true,
      priority: 1000,
      require: '^taskVariables',

      link: function(scope, element, attr, taskVariables) {
        
        function withTemplate(fn) {
          $http
            .get('directives/form-field.html', { cache: $templateCache })
            .success(function(data) {
              fn(data);
            });
        }

        withTemplate(function(tpl) {
          var newElement = $(tpl);
          var inputs = newElement.find('input');

          var elementAttrs = element.get(0).attributes;

          var ignore = {
            'form-field': true,
            'type' : true,
            'class' : true
          };

          // copy attributes from
          // template
          angular.forEach(elementAttrs, function(n) {
            var key = n.nodeName || n.name;
            var val = element.attr(key);

            console.log(key, val);

            if (!ignore[key]) {
              inputs.attr(key, val);
            }
          });

          // replace element and recompile it
          element.replaceWith(newElement);
          element = newElement;

          $compile(element)(scope);
        });

        var type = attr['type'],
            name = attr['name'],
            readOnly = (attr['readonly'] == 'readonly' || attr['readonly'] === true),
            variable = scope.$eval(attr['variable']);

        if (variable) {
          taskVariables.addVariable(variable);
        } else {
          if (!name || !type) {
            throw new Error('name or type not defined for form field');
          }

          variable = taskVariables.getVariable(name);

          if (!variable) {
            variable = { type: type, name: name };
            taskVariables.addVariable(variable);
          }
        }

        // set readonly state
        variable.readOnly = readOnly;

        scope.typeSwitch = attr['typeSwitch'];
        scope.variable = variable;

        scope.inputType = function() {
          var mapping = {
            'date': 'datetime',
            'boolean': 'checkbox',
            'number': 'number'
          };

          return mapping[variable.type] || 'text';
        };

        scope.setType = function(type) {
          variable.type = type;
        };
      }
    };
  }];

  module
    .controller('TaskVariablesController', TaskVariablesController)
    .directive('taskVariables', TaskVariablesDirective)
    .directive('formField', FormFieldDirective);
});