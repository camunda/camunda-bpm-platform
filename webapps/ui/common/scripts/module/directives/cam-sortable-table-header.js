'use strict';

function noop() {
  return;
}

var Directive = function() {
  return {
    replace: false,
    restrict: 'A',
    scope: {
      onSortInitialized: '&',
      onSortChange: '&',
      sortBy: '@defaultSortBy',
      sortOrder: '@defaultSortOrder',
      sortingId: '@'
    },
    controller: ['$scope', 'localConf', function($scope, localConf) {

      var sortingId = $scope.sortingId;
      var defaultSorting = { sortBy: $scope.sortBy, sortOrder: $scope.sortOrder };

      var onSortInitialized = $scope.onSortInitialized || noop;
      var onSortChange = $scope.onSortChange || noop;


      var sorting = loadLocal();
      onSortInitialized({sorting: sorting});

      function loadLocal() {
        return localConf.get(sortingId, defaultSorting);
      }

      function saveLocal(sorting) {
        localConf.set(sortingId, sorting);
      }

      this.changeOrder = function(column) {
        sorting.sortBy = column;
        sorting.sortOrder = (sorting.sortOrder === 'desc') ? 'asc' : 'desc';
        saveLocal(sorting);
        onSortChange({ sorting: sorting });
      };

      this.getSorting = function() {
        return sorting;
      };

    }]
  };
};

module.exports = Directive;


