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
        '$rootScope',
      function (
        $scope,
        $rootScope
      ) {

        var filtersData = $scope.tasklistData.newChild($scope);
        var query;

        /**
         * observe list of filters and pre-process
         */
        $scope.state = filtersData.observe('filters', function(filters) {

          var focused;
          $scope.totalItems = filters.length;
          each(filters, function(filter) {

            // read background color from properties
            filter.style = {
              'background-color': filter.properties.color
            };

            // auto focus first filter
            if(!focused || filter.properties.priority < focused.properties.priority) {
              focused = filter;
            }
          });

          $scope.filters = filters;
          $scope.focus(focused);

        });

        /**
         * observe the count for the current filter
         */
        filtersData.observe('taskList', function(taskList) {
          $scope.filterCount = taskList.count;
        });

        /**
         * observe the task list query to get the latest update
         */
        filtersData.observe('taskListQuery', function(taskListQuery) {
          query = angular.copy(taskListQuery);
        });

        /**
         * select a filter
         */
        $scope.focus = function(filter) {
          $scope.filterCount = undefined;

          if (filter) {

            if(filter.id !== query.id) {
              // filter changed => reset pagination
              query.firstResult = 0;
            }
            query.id = filter.id;
            
          }
          else {
            query.id = null;

          }

          filtersData.set('taskListQuery', query);
        };

        /**
         * returns true if the provided filter is the focused filter
         */
        $scope.isFocused = function(filter) {
          return filter.id === query.id;
        };

        // TODO: must be cleaned up
        $scope.edit = function(filter) {
          $rootScope.$broadcast('tasklist.filter.edit', filter);
        };

        $scope.delete = function(filter) {
          $rootScope.$broadcast('tasklist.filter.delete', filter);
        };
      }]
    };
  }];
});
