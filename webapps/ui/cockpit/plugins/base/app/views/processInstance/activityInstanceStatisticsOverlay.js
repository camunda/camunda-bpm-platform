'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/activity-instance-statistics-overlay.html', 'utf8');
var angular = require('angular');

var Configuration = function PluginConfiguration(ViewsProvider) {

  ViewsProvider.registerDefaultView('cockpit.processInstance.diagram.overlay', {
    id: 'activity-instance-statistics-overlay',
    template: template,
    controller: [
      '$scope',
      'Loaders',
      function($scope, Loaders) {
        var bpmnElement = $scope.bpmnElement,
            processData = $scope.processData.newChild($scope);

        var multiInstance;

        var stopLoading = Loaders.startLoading();

        $scope.activityInstanceStatistics = processData.observe([ 'activityIdToInstancesMap', 'activityIdToIncidentsMap'],
          function(activityIdToInstancesMap, activityIdToIncidentsMap) {

            var activityId = bpmnElement.id,
                instances = angular.copy(activityIdToInstancesMap[activityId] || []),
                incidents = angular.copy(activityIdToIncidentsMap[activityId] || []);

            stopLoading();

            multiInstance = activityIdToInstancesMap[activityId+'#multiInstanceBody'];

            var instancesMI = activityIdToInstancesMap[activityId+'#multiInstanceBody'] || [];
            var incidentsMI = activityIdToIncidentsMap[activityId+'#multiInstanceBody'] || [];

            if (instances.length || incidents.length || instancesMI.length || incidentsMI.length) {
              bpmnElement.isSelectable = true;
            }

            $scope.activityInstanceStatistics = { instances: instances, incidents: incidents, instancesMI: instancesMI, incidentsMI: incidentsMI };
          });

        var currentFilter = processData.observe('filter', function(filter) {
          currentFilter = filter;
        });

        $scope.getInstanceCount = function() {
          var count = 0;

          if($scope.activityInstanceStatistics.instances) {
            count += $scope.activityInstanceStatistics.instances.length || 0;
          }
          if($scope.activityInstanceStatistics.instancesMI) {
            count += $scope.activityInstanceStatistics.instancesMI.filter(function(instance) {
              return instance.isTransitionInstance;
            }).length || 0;
          }

          return count;
        };

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
                if(multiInstance) {
                  activityIds.splice(activityIds.indexOf(activityId + '#multiInstanceBody'), 1);
                }

              } else {
                activityIds.push(activityId);
                if(multiInstance) {
                  activityIds.push(activityId + '#multiInstanceBody');
                }
              }

            } else {
              activityIds = [ activityId ];
              if(multiInstance) {
                activityIds.push(activityId + '#multiInstanceBody');
              }
            }
          }

          newFilter.activityIds = activityIds;

          processData.set('filter', newFilter);
        };

      }],
    priority: 20
  });
};

Configuration.$inject = ['ViewsProvider'];

module.exports = Configuration;
