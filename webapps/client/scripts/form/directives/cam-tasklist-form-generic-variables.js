define([
  'angular',
  'text!./cam-tasklist-form-generic-variables.html'
], function(
  angular,
  template
) {
  'use strict';

  return [function(){

    return {

      restrict: 'EAC',

      template: template,

      link: function($scope) {

        var emptyVariable = {
          name:   '',
          value:  '',
          type:   ''
        };

        var variableTypes = $scope.variableTypes = {
          'Boolean':  'checkbox', // handled via switch in HTML template
          'Integer':  'text',
          'Double':   'text',
          'Long':     'text',
          'Short':    'text',
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

      }
    };
  }];
});
