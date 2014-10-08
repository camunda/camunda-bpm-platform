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
        tasklistData: '='
      },
      link: function(scope, element, attrs) {
         scope.searches = [];
      },
      template: template
    };
  }];
});
