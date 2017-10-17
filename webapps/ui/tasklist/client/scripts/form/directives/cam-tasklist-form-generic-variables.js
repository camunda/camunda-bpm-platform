'use strict';
var fs = require('fs');

var template = fs.readFileSync(__dirname + '/cam-tasklist-form-generic-variables.html', 'utf8');

var angular = require('camunda-commons-ui/vendor/angular');

module.exports = ['camAPI', 'Notifications', '$translate', 'unfixDate', function(camAPI, Notifications, $translate, unfixDate) {

  return {

    restrict: 'A',

    require: '^camTasklistForm',

    template: template,

    link: function($scope, $element, attrs, formController) {

    // setup ///////////////////////////////////////////////////////////

      var Task = camAPI.resource('task');
      var ProcessInstance = camAPI.resource('process-instance');
      var CaseInstance = camAPI.resource('case-instance');

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

      (function getBusinessKey() {
        var params = formController.getParams();

        var resource;

        if (params.processInstanceId) {
          resource = ProcessInstance;
        }
        else if (params.caseInstanceId) {
          resource = CaseInstance;
        }

        if (resource) {
          resource.get(params.processInstanceId || params.caseInstanceId, function(err, res) {

            $scope.readonly = true;

            if(!err && res.businessKey) {
              $scope.businessKey = res.businessKey;
            } else if (err) {
              $scope.tasklistForm.$error = {message: 'API_FAILED_BUSINESS_KEY'};
            }
          });
        }
      })();

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
              var parsedValue = value.value;

              if(value.type === 'Date') {
                parsedValue = unfixDate(parsedValue);
              }
              $scope.variables.push({
                name : name,
                value: parsedValue,
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
