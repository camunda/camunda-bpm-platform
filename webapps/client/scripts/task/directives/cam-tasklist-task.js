define([
  'text!./cam-tasklist-task.html'
], function(template) {
  'use strict';

  return [
    '$modal',
    '$rootScope',
    '$location',
    'camUID',
  function(
    $modal,
    $rootScope,
    $location,
    camUID
  ) {
    $rootScope.batchActions = {};
    $rootScope.batchActions.selected = [];

    return {
      template: template,

      link: function(scope, element) {
        scope.task = scope.task || $rootScope.currentTask;

        scope.elUID = camUID();

        element.find('.nav li').eq(0).addClass('active');
        element.find('.tab-pane').eq(0).addClass('active');

        scope.$on('tasklist.task.current', function() {
          if (scope.task && $rootScope.currentTask && scope.task.id === $rootScope.currentTask.id) {
            return;
          }
          scope.task = $rootScope.currentTask;
        });
      }
    };
  }];
});
