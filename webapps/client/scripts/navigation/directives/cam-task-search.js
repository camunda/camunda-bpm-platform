define([
  'angular',
  'text!./cam-task-search.html'
], function(
  angular,
  template
) {
  'use strict';

  return [function() {
    return {
      restrict: 'EAC',

      scope: {
        tasklistData: '=',
        tasklistApp: '='
      },

      link: function(scope, element, attrs) {
        scope.searches = [];

        scope.invalidSearch = function(search, types, operatorList) {
          return !(types.indexOf(search.type) !== -1 &&
                   operatorList.indexOf(search.operator) !== -1 &&
                   search.name &&
                   search.value);
        };
      },

      template: template
    };
  }];
});
