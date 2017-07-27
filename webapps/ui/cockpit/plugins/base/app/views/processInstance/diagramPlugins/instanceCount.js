'use strict';

var angular = require('angular');
var instanceCount = require('../../common/diagramPlugins/instanceCount');

module.exports = ['ViewsProvider',  function(ViewsProvider) {
  ViewsProvider.registerDefaultView('cockpit.processInstance.diagram.plugin', {
    id: 'activity-instance-statistics-overlay',
    overlay: [
      '$scope', 'control', 'processData', 'processDiagram', 'Loaders', '$filter', '$rootScope',
      function($scope, control, processData, processDiagram, Loaders, $filter, $rootScope) {
        var callbacks = {
          observe: observe,
          getData: getInstancesCountsForElement,
          updateOverlayNodes: updateOverlayNodes,
          isActive: isActive
        };

        instanceCount($scope, control, processData, processDiagram, Loaders, $rootScope, callbacks);

        function isActive(data) {
          return data.instanceCount > 0 || data.incidents;
        }

        function observe(callback) {
          processData.observe(
            [ 'activityIdToInstancesMap', 'activityIdToIncidentsMap'],
            function(activityIdToInstancesMap, activityIdToIncidentsMap) {
              callback([activityIdToInstancesMap, activityIdToIncidentsMap]);
            }
          );
        }

        function getInstancesCountsForElement(element, activityIdToInstancesMap, activityIdToIncidentsMap) {
          var activityId = element.id;

          var instances = angular.copy(activityIdToInstancesMap[activityId] || []);
          var incidents = angular.copy(activityIdToIncidentsMap[activityId] || []);
          var instancesMI = activityIdToInstancesMap[activityId+'#multiInstanceBody'] || [];
          var incidentsMI = activityIdToIncidentsMap[activityId+'#multiInstanceBody'] || [];
          var multiInstance = activityIdToInstancesMap[activityId+'#multiInstanceBody'];

          return {
            instanceCount: getInstanceCount({
              instances: instances,
              instancesMI: instancesMI
            }),
            incidents:  incidents.length || incidentsMI.length,
            multiInstance: multiInstance
          };
        }

        function getInstanceCount(data) {
          var count = 0;

          if(data.instances) {
            count += data.instances.length || 0;
          }

          if(data.instancesMI) {
            count += data.instancesMI.filter(function(instance) {
              return instance.isTransitionInstance;
            }).length || 0;
          }

          return count;
        }

        function updateOverlayNodes(nodes, data) {
          nodes.instancesNode.text(
            $filter('abbreviateNumber')(data.instanceCount)
          );

          if (data.instanceCount <= 0) {
            nodes.instancesNode.hide();
          } else {
            nodes.instancesNode.show();
            nodes.instancesNode.tooltip({
              title: 'Running Activity Instances',
              placement: 'top',
              animation: false
            });
          }

          if (!data.incidents) {
            nodes.incidentsNode.hide();
          } else {
            nodes.incidentsNode.show();
            nodes.incidentsNode.tooltip({
              title: 'Open Incidents',
              placement: 'top',
              animation: false
            });
          }
        }
      }
    ]
  });
}];
