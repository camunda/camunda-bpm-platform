'use strict';
var fs = require('fs');

var template = fs.readFileSync(__dirname + '/cam-tasklist-form-generic-variables.html', 'utf8');

var angular = require('camunda-commons-ui/vendor/angular');

module.exports = ['camAPI', 'Notifications', '$translate', function(camAPI, Notifications, $translate) {

  return {

    restrict: 'A',

    require: '^camTasklistForm',

    template: template,

    link: function($scope, $element, attrs, formController) {

      var Task = camAPI.resource('task');

      $scope.$watch('tasklistForm', function() {
        $scope.variablesLoaded = false;
      });

      var emptyVariable = {
        name:   '',
        value:  '',
        type:   ''
      };

      var variableTypes = $scope.variableTypes = {
        'Boolean':  'checkbox', // handled via switch in HTML template
        'Integer':  'text',
        'Long':     'text',
        'Short':    'text',
        'Double':   'text',
        'String':   'text',
        'Date':     'text'
      };

      $scope.addVariable = function() {
        var newVariable = angular.copy(emptyVariable);
        $scope.variables.push(newVariable);
      };

      $scope.removeVariable = function(delta) {
        var vars = [];

        angular.forEach($scope.variables, function(variable, d) {
          if (d != delta) {
            vars.push(variable);
          }
        });

        $scope.variables = vars;
      };

      $scope.getVariableNames = function() {
        return $scope.variables.map(function(variable) {
          return variable.name;
        });
      };

      $scope.loadVariables = function() {
        $scope.variablesLoaded = true;
        Task.formVariables({
          id: formController.getParams().taskId,
          deserializeValues: false
        }, function(err, result) {
          if(err) {
            $scope.variablesLoaded = false;
            return $translate('LOAD_VARIABLES_FAILURE').then(function(translated) {
              Notifications.addError({
                status: translated,
                message: err.message,
                scope: $scope
              });
            });
          }

          var variableAdded = false;
          angular.forEach(result, function(value, name) {
            if(variableTypes[value.type]) {
              $scope.variables.push({
                name : name,
                value: value.value,
                type:  value.type,
                fixedName : true
              });
              variableAdded = true;
            }
          });
          if(!variableAdded) {
            $translate('NO_TASK_VARIABLES').then(function(translated) {
              Notifications.addMessage({
                duration: 5000,
                status: translated,
                scope: $scope
              });
            });
          }
        });
      };
    }
  };
}];
