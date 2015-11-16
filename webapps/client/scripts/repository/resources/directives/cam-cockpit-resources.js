define([
  'angular',
  'text!./cam-cockpit-resources.html'
], function(
  angular,
  template
) {
  'use strict';

  var $ = angular.element;

  return [function(){

    return {

      restrict: 'A',
      scope: {
        repositoryData: '='
      },

      template: template,

      controller: [
        '$scope',
        '$location',
        '$timeout',
        'search',
      function(
        $scope,
        $location,
        $timeout,
        search
      ) {

        var resourcesData = $scope.repositoryData.newChild($scope);

        // utilities /////////////////////////////////////////////////////////////////

        var updateSilently = function (params) {
          search.updateSilently(params);
        }

        var getPropertyFromLocation = function (property) {
          var search = $location.search() || {};
          return search[property] || null;
        }


        // observe data //////////////////////////////////////////////////////////////

        $scope.state = resourcesData.observe('resourceId', function (resourceId) {
          $scope.currentResourceId = resourceId.resourceId;
        });

        $scope.state = resourcesData.observe('resources', function (resources) {
          $scope.resources = resources.map(function (resource) {
            var parts = (resource.name || resource.id).split('/');
            resource._filename = parts.pop();
            resource._filepath = parts.join('/');
            return resource;
          });
        });


        // selection //////////////////////////////////////////////////////////////////

        $scope.focus = function ($event, resource) {
          if ($event) {
            $event.preventDefault();
          }

          var resourceId = resource.id;

          if ($scope.currentResourceId === resourceId) {
            updateSilently({
              resource: resourceId,
              resourceName: null,
              editMode: null
            });
          }
          else {
            updateSilently({
              resource: resourceId,
              resourceName: null,
              viewbox: null,
              editMode: null
            });
          }

          resourcesData.changed('resourceId');
        };

        var selectNextResource = function() {
          for(var i = 0; i < $scope.resources.length - 1; i++) {
            if($scope.resources[i].id === $scope.currentResourceId) {
              return $scope.focus(null, $scope.resources[i+1]);
            }
          }
        };

        var selectPreviousResource = function() {
          for(var i = 1; i < $scope.resources.length; i++) {
            if($scope.resources[i].id === $scope.currentResourceId) {
              return $scope.focus(null, $scope.resources[i-1]);
            }
          }
        };

        $scope.handleKeydown = function($event) {
          if($event.keyCode === 40) {
            $event.preventDefault();
            selectNextResource($event);
          }
          else if($event.keyCode === 38) {
            $event.preventDefault();
            selectPreviousResource();
          }
          // wait for angular to update the classes and scroll to the newly selected task
          $timeout(function(){
            var $el = $($event.target).find('li.active')[0];
            if ($el) {
              $el.scrollIntoView(false);
            }
          });
        };

      }]
    };
  }];
});
