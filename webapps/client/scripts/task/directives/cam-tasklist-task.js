define([
  'text!./cam-tasklist-task.html'
], function(template) {
  'use strict';

  return [
    '$modal',
    '$rootScope',
    'camUID',
  function($modal,
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

        // scope.$watch('currentTask', function() {
        scope.$on('tasklist.task.current', function() {
          if (
            !$rootScope.currentTask ||
            (scope.task && scope.task.id === $rootScope.currentTask.id)
          ) {
            return;
          }

          scope.task = $rootScope.currentTask;
          console.info('Current task is now', scope.task);
        });
      },
      template: template
    };
  }];
});
