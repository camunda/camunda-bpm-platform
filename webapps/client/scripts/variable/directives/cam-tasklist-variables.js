// RequireJS dependencies
define([
  'angular',
  'text!./cam-tasklist-variables.html',
  'text!../modals/cam-tasklist-variables-detail-modal.html'
], function(
  angular,
  template,
  modalTemplate
) {
  'use strict';

  // AngularJS DI
  return [
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

        scope.toggle = function($event) {
          scope.expanded = !scope.expanded;
          if ($event && $event.preventDefault) {
            $event.preventDefault();
          }
          $event.stopPropagation();
        };

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
});
