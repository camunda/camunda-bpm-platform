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

  var FormScriptDirective = [ '$injector', function($injector) {
    return {
      restrict: 'EA',
      require: '^taskVariables',
      terminal: true,
      link: function(scope, element, attr) {

        if (attr.type != 'text/form-script') {
          return;
        }

        // IE is not consistent, in scripts we have to read .text but in other nodes we have to read .textContent
        var extensionScript = element[0].text;

        (function($scope) {

          // hook to create the service with injection
          var inject = function(extensions) {
            // if result is an array or function we expect
            // an injectable service
            if (angular.isFunction(extensions) || angular.isArray(extensions)) {
              $injector.instantiate(extensions, { $scope: scope });
            } else {
              throw new Error('Must call inject(array|fn)');
            }
          };

          // may bind against $scope directly or use
          // inject([ '$scope', '$http', function($scope, $http) { ... }])
          eval(extensionScript);

        })(scope);
      }
    };
  }];

  var FormFieldDirective = [
       '$http', '$templateCache', '$compile', '$controller',
       function($http, $templateCache, $compile, $controller) {

    return {
      scope: true,
      priority: 1000,
      terminal: true,
      require: '^taskVariables',

      compile: function(element, attr) {

        /** this function is used to parse the values provided as
         * child <OPTION> elements of a <SELECT> element:
         *
         * <select ...>
         *   <option value="value1">label1</option>
         *   <option value="value2">label2</option>
         *   <option value="value3" label="label3">
         *   <option>valueLabel4</option>
         * </select>
         *
         * retuns a list of value / label objects.
         */
        function parseOptions(element) {
          var options=[];

          angular.forEach($("option", element), function(option) {

            var value = $(option).attr("value")
            if(!value) {
              value = $(option).text()
            }

            var label = $(option).attr("label")
            if(!label) {
              label = $(option).text()
            }

            options.push({
              'value': value,
              'label': label
            });
          });

          return options;
        };

        function parseOptionsObject(optionsObject) {
          var result = [];
          for(prop in optionsObject) {
            result.push({'label': optionsObject[prop], 'value': prop});
          }
          return result;
        };

        function parseOptionsList(optionsList) {
          var result = [];
          angular.forEach(optionsList, function(option) {
            result.push({'label':option, 'value':option});
          });
          return result;
        };

        function parseAttributes(element) {
          var attributes = { };

          var elementAttrs = element.get(0).attributes;

          var ignoreAttrs = {
            'form-field': true,
            'type' : true,
            'class' : true
          };

          // parse attributes from template
          angular.forEach(elementAttrs, function(n) {
            var key = n.nodeName || n.name;

            if (!ignoreAttrs[key]) {
              attributes[key] = element.attr(key);
            }
          });

          angular.forEach(attributes, function(val, key) {
            element.removeAttr(key);
          });

          return attributes;
        }

        var controlType = element.prop("tagName");
        if("INPUT"==controlType) {
          controlType += "-" + element.attr("type");
        }
        controlType = controlType.toUpperCase();

        var elementAttr = parseAttributes(element);
        var options = parseOptions(element);

        return function(scope, element, attr, taskVariables) {

          function withTemplate(fn) {
            $http
              .get('directives/form-field.html', { cache: $templateCache })
              .success(function(data) {
                fn(data);
              });
          }

          withTemplate(function(tpl) {
            var newElement = $(tpl);
            var inputs = newElement.find('input, select, textarea');

            // add attributes to element
            angular.forEach(elementAttr, function(val, key) {
              inputs.attr(key, val);
            });

            // replace element and recompile it
            element.replaceWith(newElement);
            element = newElement;

            $compile(element)(scope);
          });

          var type = attr['type'],
              name = attr['name'],
              formValues = attr['formValues'],
              readOnly = (attr['readonly'] == 'readonly' || attr['readonly'] === true),
              variable = scope.$eval(attr['variable']);

          if (variable) {
            taskVariables.addVariable(variable);
          } else {
            if ((!name || !type) && controlType != "TEXTAREA") {
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

          // the type of control which should be used.
          scope.controlType = controlType;

          // options for select boxes
          scope.options = options;

          // bind additional options from process variable
          if(!!formValues) {
            // watch value of variable since variables are loaded async to form processing
            scope.$watch(function() {return taskVariables.getVariable(formValues);}, function(additionalOptions) {
              if(!!additionalOptions) {
                if($.isArray(additionalOptions.value)) {
                  scope.options = scope.options.concat(parseOptionsList(additionalOptions.value));
                } else {
                  scope.options = scope.options.concat(parseOptionsObject(additionalOptions.value));
                }
              }
            });
          }

          // the type of the value provided by the control
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
      }
    };
  }];

  module
    .controller('TaskVariablesController', TaskVariablesController)
    .directive('taskVariables', TaskVariablesDirective)
    .directive('formField', FormFieldDirective)
    .directive('formScript', FormScriptDirective);
});
