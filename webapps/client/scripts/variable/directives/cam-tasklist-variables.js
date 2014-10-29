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
    '$http',
    'Uri',
    'details',
  function(
    $scope,
    $modalInstance,
    $http,
    Uri,
    details
  ) {

    $scope.$on('$locationChangeSuccess', function() {
      $scope.$dismiss();
    });

    $scope.value = null;
    $scope.valueDeserialized = null;
    $scope.deserializationError = null;
    $scope.type = null;
    $scope.dataFormat = null;
    $scope.variable = details;
    $scope.selectedTab = 'serialized';

    switch ($scope.variable.type) {
      case 'Object':
        $scope.type = $scope.variable.valueInfo.objectTypeName;
        $scope.value = $scope.variable.value;
        $scope.dataFormat = $scope.variable.valueInfo.serializationDataFormat;

        // attempt fetching the deserialized value
        $http({
          method: 'GET', 
          url: Uri.appUri('engine://engine/:engine'+$scope.variable._links.self.href)
        }).success(function(data, status) {
          $scope.valueDeserialized = JSON.stringify(data.value);
        }).error(function(data, status) {
          $scope.deserializationError = data.message;
        });

        break;

      default:
        $scope.value = $scope.variable.value;
    }

    $scope.selectTab = function(tab) {
      $scope.selectedTab = tab;
    };

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
          // than calling a function which would iterate every time.
          angular.forEach(scope.variables, function(variable) {
            scope.variablesByName[variable.name] = variable;
          });

          scope.shownVariablesCount = Object.keys(scope.variablesByName).length;
        }
      }
    };
  }];
});
