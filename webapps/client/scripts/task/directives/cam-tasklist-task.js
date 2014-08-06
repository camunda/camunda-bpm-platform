define([
  'text!./cam-tasklist-task.html'
], function(template) {
  'use strict';

  return [
    '$modal',
    '$rootScope',
    'camUID',
  function(
    $modal,
    $rootScope,
    camUID
  ) {
    $rootScope.batchActions = {};
    $rootScope.batchActions.selected = [];

    return {
      link: function(scope, element) {
        scope.task = scope.task || $rootScope.currentTask;

        scope.elUID = camUID();

        element.find('.nav li').eq(0).addClass('active');
        element.find('.tab-pane').eq(0).addClass('active');

        scope.$on('tasklist.task.current', function() {
          if (
            !$rootScope.currentTask ||
            (scope.task && scope.task._links.self.href === $rootScope.currentTask._links.self.href)
          ) {
            return;
          }

          scope.task = $rootScope.currentTask;
        });
      },
      template: template
    };
  }];
});
