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
        'camAPI',
      function (
        $scope,
        $q,
        camAPI
      ) {

        var Filter = camAPI.resource('filter');
        var filtersData = $scope.filtersData = $scope.tasklistData.newChild($scope);
        var query;

        /**
         * observe list of filters and pre-process
         */
        $scope.state = filtersData.observe(['filters', 'taskListQuery', function(filters, taskListQuery) {        

          $scope.totalItems = filters.length;
          
          var focused,
              filterId = query.id;

          for (var i = 0, filter; !!(filter = filters[i]); i++) {
            // read background color from properties
            filter.style = {
              'background-color': filter.properties.color
            };

            if (filterId) {
              if (filterId === filter.id) {
                focused = filter;
                break;
              }
            }
            else {
              // auto focus first filter
              if(!focused || filter.properties.priority < focused.properties.priority) {
                focused = filter;
              }
            }           
          }

          $scope.filters = filters;
          $scope.focus(focused);

        }]);

        /**
         * observe the count for the current filter
         */
        filtersData.observe('taskList', function(taskList) {
          $scope.filterCount = taskList.count;
        });

        /**
         * observe the count for the current filter
         */
        filtersData.observe('taskListQuery', function(taskListQuery) {
          query = taskListQuery;
        });

        /**
         * observe list of filters to set the background-color on a filter
         */
        $scope.state = filtersData.observe('filters', function(filters) {        

          $scope.totalItems = filters.length;

          for (var i = 0, filter; !!(filter = filters[i]); i++) {
            // read background color from properties
            filter.style = {
              'background-color': filter.properties.color
            };
       
          }

          $scope.filters = filters;

        });

        /**
         * observe list of filters and taskListQuery to set the focus
         */
        $scope.state = filtersData.observe(['filters', 'taskListQuery', function(filters, taskListQuery) {        
        
          var focused,
              filterId = query.id;

          for (var i = 0, filter; !!(filter = filters[i]); i++) {

            if (filterId) {
              if (filterId === filter.id) {
                focused = filter;
                break;
              }
            }
            else {
              // auto focus first filter
              if(!focused || filter.properties.priority < focused.properties.priority) {
                focused = filter;
              }
            }           
          }

          $scope.focus(focused);

        }]);

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

          filtersData.set('taskListQuery', angular.copy(query));
        };

        /**
         * returns true if the provided filter is the focused filter
         */
        $scope.isFocused = function(filter) {
          return filter.id === query.id;
        };

      }]
    };
  }];
});
