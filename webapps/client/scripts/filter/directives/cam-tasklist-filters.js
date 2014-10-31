define([
  'text!./cam-tasklist-filters.html',
  'angular'
], function(
  template,
  angular
) {
  'use strict';
  var $ = angular.element;
  var each = angular.forEach;

  return [function() {

    return {

      restrict: 'EAC',
      scope: {
        tasklistData: '='
      },

      template: template,

      controller: [
        '$scope',
        '$q',
        '$location',
        'search',
        'camAPI',
      function (
        $scope,
        $q,
        $location,
        search,
        camAPI
      ) {

        var Filter = camAPI.resource('filter');
        var filtersData = $scope.filtersData = $scope.tasklistData.newChild($scope);

        /**
         * observe the count for the current filter
         */
        filtersData.observe('taskList', function(taskList) {
          $scope.filterCount = taskList.count;
        });

        /**
         * observe list of filters to set the background-color on a filter
         */
        $scope.state = filtersData.observe('filters', function(filters) {

          $scope.totalItems = filters.length;

          for (var i = 0, filter; !!(filter = filters[i]); i++) {
            // read background color from properties
            filter.style = {
              'border-color': filter.properties.color,
              'background-color': filter.properties.color
            };

          }

          $scope.filters = filters;

        });

        filtersData.observe('currentFilter', function (currentFilter) {
          $scope.currentFilter = currentFilter;
        });

        /**
         * select a filter
         */
        $scope.focus = function(filter) {
          $scope.filterCount = undefined;

          search.updateSilently({
            filter: filter.id
          });

          filtersData.changed('currentFilter');
        };

        /**
         * returns true if the provided filter is the focused filter
         */
        $scope.isFocused = function(filter) {
          return filter.id === $scope.currentFilter.id;
        };

      }]
    };
  }];
});
