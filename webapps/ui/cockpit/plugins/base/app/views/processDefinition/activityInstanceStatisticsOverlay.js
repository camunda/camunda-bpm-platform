'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/activity-instance-statistics-overlay.html', 'utf8');
var angular = require('angular');

module.exports = ['ViewsProvider',  function(ViewsProvider) {

  ViewsProvider.registerDefaultView('cockpit.processDefinition.diagram.overlay', {
    id: 'activity-instance-statistics-overlay',
    template: template,
    controller: [
      '$scope',
      'Loaders',
      function($scope, Loaders) {

        var bpmnElement = $scope.bpmnElement,
            processData = $scope.processData.newChild($scope);

        var stopLoading = Loaders.startLoading();

        processData.provide('activityInstance', ['activityInstanceStatistics', function(activityInstanceStatistics) {
          for (var i = 0; i < activityInstanceStatistics.length; i++) {
            var current = activityInstanceStatistics[i];
            if (current.id === bpmnElement.id) {
              return current;
            }
          }
          return null;
        }]);

        processData.provide('activityInstanceMI', ['activityInstanceStatistics', function(activityInstanceStatistics) {
          for (var i = 0; i < activityInstanceStatistics.length; i++) {
            var current = activityInstanceStatistics[i];
            if (current.id === bpmnElement.id + '#multiInstanceBody') {
              return current;
            }
          }
          return null;
        }]);

        $scope.activityInstance = processData.observe('activityInstance', function(activityInstance) {
          if (activityInstance) {
            bpmnElement.isSelectable = true;
          }

          stopLoading();
          $scope.activityInstance = activityInstance;
        });
        $scope.activityInstanceMI = processData.observe('activityInstanceMI', function(activityInstance) {
          if (activityInstance) {
            bpmnElement.isSelectable = true;
          }

          stopLoading();
          $scope.activityInstanceMI = activityInstance;
        });

        var currentFilter = processData.observe('filter', function(filter) {
          currentFilter = filter;
        });

        $scope.selectRunningInstances = function(e) {
          var newFilter = angular.copy(currentFilter),
              ctrl = e.ctrlKey,
              activityId = bpmnElement.id,
              activityIds = angular.copy(newFilter.activityIds) || [],
              idx = activityIds.indexOf(activityId),
              selected = idx !== -1;

          if (!activityId) {
            activityIds = null;

          } else {

            if (ctrl) {
              if (selected) {
                activityIds.splice(idx, 1);

              } else {
                activityIds.push(activityId);
              }

            } else {
              activityIds = [ activityId ];
            }
          }

          newFilter.activityIds = activityIds;

          processData.set('filter', newFilter);
        };

      }],
    priority: 20
  });
}];
