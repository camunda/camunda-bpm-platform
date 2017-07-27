'use strict';

var instanceCount = require('../../common/diagramPlugins/instanceCount');

module.exports = ['ViewsProvider',  function(ViewsProvider) {
  ViewsProvider.registerDefaultView('cockpit.processDefinition.diagram.plugin', {
    id: 'activity-instance-statistics-overlay',
    overlay: [
      '$scope', 'control', 'processData', 'processDiagram', 'Loaders', 'get', '$filter', '$rootScope',
      function($scope, control, processData, processDiagram, Loaders, get, $filter, $rootScope) {
        var callbacks = {
          observe: observe,
          getData: getInstancesCountsForElement,
          updateOverlayNodes: updateOverlayNodes,
          isActive: isActive
        };

        instanceCount($scope, control, processData, processDiagram, Loaders, $rootScope, callbacks);

        function observe(callback) {
          processData.observe(['activityInstanceStatistics'], function(activityInstanceStatistics) {
            callback([activityInstanceStatistics]);
          });
        }

        function isActive(data) {
          return data.instances || data.incidents;
        }

        function getInstancesCountsForElement(element, activityInstanceStatistics) {
          var stats = getStatsWithId(activityInstanceStatistics, element.id);
          var statsMi = getStatsWithId(activityInstanceStatistics, element.id + '#multiInstanceBody');

          return {
            instances: get(stats, ['instances'], 0) + get(statsMi, ['instances'], 0),
            incidents: get(stats, ['incidents', 'length'], 0) + get(statsMi, ['incidents', 'length'], 0)
          };
        }

        function getStatsWithId(activityInstanceStatistics, id) {
          return activityInstanceStatistics.filter(function(entry) {
            return entry.id === id;
          })[0];
        }
        function updateOverlayNodes(nodes, data) {
          nodes.instancesNode.text(
            $filter('abbreviateNumber')(data.instances)
          );

          if (data.instances <= 0) {
            nodes.instancesNode.hide();
          } else {
            nodes.instancesNode.show();
            nodes.instancesNode.tooltip({
              title: 'Running Activity Instances',
              placement: 'top',
              animation: false
            });
          }

          if (data.incidents) {
            nodes.incidentsNode.show();
            nodes.incidentsNode.tooltip({
              title: 'Open Incidents',
              placement: 'top',
              animation: false
            });
          } else {
            nodes.incidentsNode.hide();
          }
        }
      }
    ]
  });
}];
