// RequireJS dependencies
define([
  'angular',
  'moment',
  'text!./cam-tasklist-variables.html',
  'text!./cam-tasklist-variables-detail-modal.html'
], function(
  angular,
  moment,
  template,
  modalTemplate
) {
  'use strict';



  var modalCtrl = [
    '$scope',
    '$modalInstance',
    'details',
  function(
    $scope,
    $modalInstance,
    details
  ) {

    $scope.$on('$locationChangeSuccess', function() {
      $scope.$dismiss();
    });

    $scope.value = null;
    $scope.type = null;
    $scope.variable = details;

    switch ($scope.variable.type) {
      case 'Serializable':
        $scope.value = JSON.stringify($scope.variable.value.object, null, 2);
        $scope.type = $scope.variable.value.type;
        break;

      case 'Object':
        $scope.type = $scope.variable.serializationConfig.rootType;
        $scope.value = $scope.variable.value;

        if ($scope.type.toLowerCase().indexOf('json') > -1 && typeof $scope.value === 'string') {
          $scope.value = JSON.stringify(JSON.parse($scope.value), null, 2);
        }
        break;

      default:
        $scope.value = $scope.variable.value;
    }
  }];




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

        // scope.selectValue = function($event) {
        //   var $label = angular.element($event.target);
        //   var $value = $label.parents('.variable-item').find('.variable-value');
        //   if ($value.length) {
        //     var range = $window.document.createRange();
        //     range.selectNode($value[0]);
        //   }
        // };

        scope.toggle = function($event) {
          scope.expanded = !scope.expanded;
          if ($event && $event.preventDefault) {
            $event.preventDefault();
          }
        };

        scope.showValue = function(variable) {
          $modal.open({
            template: modalTemplate,

            windowClass: 'variable-modal-detail',

            resolve: {
              details: function() { return variable; }
            },

            controller: modalCtrl
          });
        };

        scope.download = function(variable) {
          var link = variable._links.self.href +'/data';
          link = Uri.appUri('engine://engine/:engine'+ link);
          $window.open(link, 'download');
        };

        if (scope.filterProperties) {
          scope.variableDefinitions = scope.filterProperties.variables || {};

          // building an object on which keys are name of variables is more efficient
          // than calling a function who would iterate every time.
          angular.forEach(scope.variables, function(variable) {
            scope.variablesByName[variable.name] = variable;
          });

          scope.shownVariablesCount = Object.keys(scope.variablesByName).length;
        }
      }
    };
  }];
});
