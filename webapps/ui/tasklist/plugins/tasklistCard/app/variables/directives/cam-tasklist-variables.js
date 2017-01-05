'use strict';
var fs = require('fs');

var template = fs.readFileSync(__dirname + '/cam-tasklist-variables.html', 'utf8');
var modalTemplate = fs.readFileSync(__dirname + '/../modals/cam-tasklist-variables-detail-modal.html', 'utf8');

var angular = require('camunda-commons-ui/vendor/angular');

  // AngularJS DI
module.exports = [
  '$modal',
  '$window',
  'Uri',
  function(
    $modal,
    $window,
    Uri
  ) {

    return {
      template: template,

      scope: {
        variables:        '=',
        filterProperties: '='
      },

      link: function(scope) {
        scope.variableDefinitions = [];
        scope.variablesByName = {};
        scope.expanded = false;
        scope.shownVariablesCount = 0;

        scope.showValue = function(variable, $event) {
          $event.preventDefault();
          $event.stopPropagation();
          $modal.open({
            template: modalTemplate,

            windowClass: 'variable-modal-detail',

            resolve: {
              details: function() { return variable; }
            },

            controller: 'camTasklistVariablesDetailsModalCtrl'
          });
        };

        scope.download = function(variable, $event) {
          $event.preventDefault();
          $event.stopPropagation();
          var link = variable._links.self.href +'/data';
          link = Uri.appUri('engine://engine/:engine'+ link);
          $window.open(link, 'download');
        };

        if (scope.filterProperties) {
          scope.variableDefinitions = scope.filterProperties.variables || {};

          // building an object on which keys are name of variables is more efficient
          // than calling a function which would iterate every time.
          angular.forEach(scope.variables, function(variable) {
            scope.variablesByName[variable.name] = variable;
          });

          scope.shownVariablesCount = Object.keys(scope.filterProperties.showUndefinedVariable ? scope.variableDefinitions : scope.variablesByName).length;
        }
      }
    };
  }];
