'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/sortable-table-head.html', 'utf8');

var Directive = function() {
  return {
    replace: false,
    restrict: 'AE',
    scope: {
      headColumns: '=?',
      onSortChange: '&',
      defaultSort: '=?'
    },
    template: template,
    controller: ['$scope',
      function($scope) {

        // Order Icons
        $scope.orderClass = function(forColumn) {
          forColumn = forColumn || $scope.defaultSort.sortBy;
          var icons = {
            none: 'minus',
            desc: 'chevron-down',
            asc: 'chevron-up'
          };
          return 'glyphicon-' + (icons[forColumn === $scope.defaultSort.sortBy ? $scope.defaultSort.sortOrder : 'none']);
        };

        // On-click function to order Columns
        $scope.changeOrder = function(column) {
          $scope.defaultSort.sortBy    = column;
          $scope.defaultSort.sortOrder = ($scope.defaultSort.sortOrder === 'desc') ? 'asc' : 'desc';
          $scope.onSortChange({ sortObj: $scope.defaultSort });
        };

      }],
    link: function() {}
  };
};

module.exports = Directive;


