'use strict';
var fs = require('fs');

var template = fs.readFileSync(__dirname + '/cam-tasklist-filters.html', 'utf8');

var noop = function() {};

module.exports = [function() {

  return {

    restrict: 'A',
    scope: {
      filtersData: '=',
      openModal: '&'
    },

    template: template,

    controller: [
      '$scope',
      'search',
      function(
        $scope,
        search
      ) {

        var filtersData = $scope.filtersData = $scope.filtersData.newChild($scope);

        $scope.openModal = $scope.openModal() || noop;

        // observe ////////////////////////////////////////////////////////////////////////////////

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

          for (var i = 0, filter; (filter = filters[i]); i++) {
            filter.style = {
              'z-index': filters.length + 10 - i
            };
          }

          $scope.filters = filters;

        });

        filtersData.observe('currentFilter', function(currentFilter) {
          $scope.currentFilter = currentFilter;
        });

        // selection ////////////////////////////////////////////////////////////////

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
