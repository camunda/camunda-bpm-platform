'use strict';

var instanceCount = require('../../common/diagramPlugins/instanceCount');

var fs = require('fs');
var template = fs.readFileSync(__dirname + '/overlayAction.html', 'utf8');

module.exports = ['ViewsProvider',  function(ViewsProvider) {
  ViewsProvider.registerDefaultView('cockpit.processDefinition.diagram.action', {
    id: 'activity-instance-statistics-overlay',
    template:template,
    controller: [
      '$scope', 'Loaders', 'get','$filter', '$rootScope', '$translate','configuration',
      function($scope, Loaders, get, $filter, $rootScope, $translate, configuration
      ) {

        $scope.toggle = configuration.getRuntimeActivityInstanceMetrics();
        $scope.isLoading = false;

        showOverlays();
        $scope.toggleOverlay = function() {
          $scope.toggle = !$scope.toggle;
          if($scope.toggle) {
            showOverlays();
          } else {
            removeOverlays();
          }
        };

        function toggleIsLoading() {
          $scope.isLoading = !$scope.isLoading;
        }

        function showOverlays() {
          $scope.isLoading = true;

          var callbacks = {
            observe: observe,
            getData: getInstancesCountsForElement,
            updateOverlayNodes: updateOverlayNodes,
            isActive: isActive,
            toggleIsLoading: toggleIsLoading
          };

          if(!$scope.processDiagram) {
            $scope.processData.observe('processDiagram', function(processDiagram) {
              $scope.processDiagram = processDiagram;
              instanceCount($scope, $scope.viewer, $scope.processData, $scope.processDiagram, Loaders, $rootScope, callbacks);
            });
          } else {
            instanceCount($scope, $scope.viewer, $scope.processData, $scope.processDiagram, Loaders, $rootScope, callbacks);
          }
        }

        function removeOverlays() {
          Array.from(document.getElementsByClassName('djs-overlay')).forEach( function(element) { element.remove(); });
        }


        function observe(callback) {
          $scope.processData.observe(['activityInstanceStatistics'], function(activityInstanceStatistics) {
            callback([activityInstanceStatistics]);
          });
        }

        function isActive(data) {
          return data.instances || data.incidents;
        }

        var getIncidentCount = function(incidents) {
          if(!incidents) {
            return 0;
          }

          return incidents.reduce(function(sum, incident) {
            return sum + incident.incidentCount;
          }, 0);
        };


        function getInstancesCountsForElement(element, activityInstanceStatistics) {
          var stats = getStatsWithId(activityInstanceStatistics, element.id);
          var statsMi = getStatsWithId(activityInstanceStatistics, element.id + '#multiInstanceBody');

          var statsIncidents = get(stats, ['incidents'], []);
          var statsMiIncidents = get(statsMi, ['incidents'], []);
          var incidents = statsIncidents.concat(statsMiIncidents);
          var incidentsCount = getIncidentCount(incidents);

          return {
            instances: get(stats, ['instances'], 0) + get(statsMi, ['instances'], 0),
            incidents: incidentsCount
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

          nodes.incidentsNode.text(
            $filter('abbreviateNumber')(data.incidents)
          );

          if (data.instances <= 0) {
            nodes.instancesNode.hide();
          } else {
            nodes.instancesNode.show();
            nodes.instancesNode.tooltip({
              container: 'body',
              title: $translate.instant('PLUGIN_ACTIVITY_INSTANCE_RUNNING_ACTIVITY_INSTANCES'),
              placement: 'top',
              animation: false
            });
          }

          if (data.incidents) {
            nodes.incidentsNode.show();
            nodes.incidentsNode.tooltip({
              container: 'body',
              title: $translate.instant('PLUGIN_ACTIVITY_INSTANCE_OPEN_INCIDENTS'),
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
