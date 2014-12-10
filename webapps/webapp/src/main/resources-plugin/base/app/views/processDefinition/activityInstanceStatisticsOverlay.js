/* global define: false */
define(['text!./activity-instance-statistics-overlay.html'], function(template) {
  'use strict';

  return [ 'ViewsProvider', function(ViewsProvider) {

    ViewsProvider.registerDefaultView('cockpit.processDefinition.diagram.overlay', {
      id: 'activity-instance-statistics-overlay',
      template: template,
      controller: [
               '$scope',
      function ($scope) {

        var bpmnElement = $scope.bpmnElement,
            processData = $scope.processData.newChild($scope);

        processData.provide('activityInstance', ['activityInstanceStatistics', function (activityInstanceStatistics) {
          for (var i = 0; i < activityInstanceStatistics.length; i++) {
            var current = activityInstanceStatistics[i];
            if (current.id === bpmnElement.id) {
              return current;
            }
          }
          return null;
        }]);

        $scope.activityInstance = processData.observe('activityInstance', function(activityInstance) {
          if (activityInstance) {
            bpmnElement.isSelectable = true;
          }

          $scope.activityInstance = activityInstance;
        });

      }],
      priority: 20
    });
  }];
});
