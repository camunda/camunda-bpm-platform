'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/cam-sortable-table-column.html', 'utf8');

var Directive = function() {
  return {
    replace: false,
    restrict: 'A',
    scope: {
      column: '@sortByProperty'
    },
    transclude: true,
    require: '^^camSortableTableHeader',
    template: template,
    link: function($scope, element, attrs, ctrl) {

      // Order Icons
      $scope.orderClass = function(forColumn) {
        var sorting = ctrl.getSorting();
        forColumn = forColumn || sorting.sortBy;
        var icons = {
          none: 'minus',
          desc: 'chevron-down',
          asc: 'chevron-up'
        };
        return 'glyphicon-' + (icons[forColumn === sorting.sortBy ? sorting.sortOrder : 'none']);
      };

      // On-click function to order Columns
      $scope.changeOrder = function(column) {
        ctrl.changeOrder(column);
      };

    }
  };
};

module.exports = Directive;


