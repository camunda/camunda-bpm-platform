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
        // Set default sorting
        $scope.sortObj = $scope.defaultSort;

        // Order Icons

        $scope.orderClass = function(forColumn) {
          forColumn = forColumn || $scope.sortObj.sortBy;
          var icons = {
            none: 'minus',
            desc: 'chevron-down',
            asc: 'chevron-up'
          };
          return 'glyphicon-' + (icons[forColumn === $scope.sortObj.sortBy ? $scope.sortObj.sortOrder : 'none']);
        };

        // On-click function to order Columns
        $scope.changeOrder = function(column) {
          $scope.sortObj.sortBy    = column;
          $scope.sortObj.sortOrder = ($scope.sortObj.sortOrder === 'desc') ? 'asc' : 'desc';

          // pass sorting to updateView function in parent scope.
          $scope.onSortChange({
            sortObj: $scope.sortObj
          });
        };
      }],
    link: function() {}
  };
};

module.exports = Directive;


