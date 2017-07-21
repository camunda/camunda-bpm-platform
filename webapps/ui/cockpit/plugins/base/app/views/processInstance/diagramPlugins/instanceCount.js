'use strict';

var fs = require('fs');
var angular = require('angular');

var template = fs.readFileSync(__dirname + '/instanceCount.html', 'utf8');

module.exports = ['ViewsProvider',  function(ViewsProvider) {
  ViewsProvider.registerDefaultView('cockpit.processInstance.diagram.plugin', {
    id: 'activity-instance-statistics-overlay',
    overlay: [
      '$scope', 'control', 'processData', 'processDiagram', 'Loaders',
      function($scope, control, processData, processDiagram, Loaders) {
        var viewer = control.getViewer();
        var overlays = viewer.get('overlays');
        var stopLoading = Loaders.startLoading();
        var overlaysNodes = {};

        processData.observe([ 'activityIdToInstancesMap', 'activityIdToIncidentsMap'], function(activityIdToInstancesMap, activityIdToIncidentsMap) {
          stopLoading();

          Object
            .keys(processDiagram.bpmnElements)
            .forEach(function(key) {
              var element = processDiagram.bpmnElements[key];
              var data = getInstancesCountsForElement(element, activityIdToInstancesMap, activityIdToIncidentsMap);
              var nodes;

              if (data.instanceCount > 0 || data.indicents) {
                if (!overlaysNodes[element.id]) {
                  nodes = getOverlayNodes(element, data);

                  overlays.add(element.id, {
                    position: {
                      bottom: 0,
                      left: 0
                    },
                    show: {
                      minZoom: -Infinity,
                      maxZoom: +Infinity
                    },
                    html: nodes.html
                  });

                  overlaysNodes[element.id] = nodes;
                }
              }

              if (overlaysNodes[element.id]) {
                updateOverlayNodes(overlaysNodes[element.id], data);
              }
            });
        });

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

        function getOverlayNodes(element, data) {
          var html = angular.element(template);
          var clickListener = selectRunningInstances.bind(null, element, data);
          var nodes = {
            html: html,
            instancesNode: html.find('.instance-count'),
            incidentsNode: html.find('.instance-incidents')
          };
          
          html.on('click', clickListener);

          $scope.$on('$destroy', function() {
            html.off('click', clickListener);
          });

          return nodes;
        }

        function updateOverlayNodes(nodes, data) {
          nodes.instancesNode.text(data.instanceCount);

          if (data.instanceCount <= 0) {
            nodes.instancesNode.hide();
          } else {
            nodes.instancesNode.show();
          }

          if (!data.incidents) {
            nodes.incidentsNode.hide();
          } else {
            nodes.incidentsNode.show();
          }
        }

        var currentFilter = processData.observe('filter', function(filter) {
          currentFilter = filter;
        });

        function selectRunningInstances(element, data, event) {
          var newFilter = angular.copy(currentFilter);
          var ctrl = event.ctrlKey;
          var activityId = element.id;
          var activityIds = angular.copy(newFilter.activityIds) || [];
          var idx = activityIds.indexOf(activityId);
          var selected = idx !== -1;
          var multiInstance = data.multiInstance;

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
              activityIds = [activityId];
              if(multiInstance) {
                activityIds.push(activityId + '#multiInstanceBody');
              }
            }
          }

          newFilter.activityIds = activityIds;

          $scope.$apply(function() {
            processData.set('filter', newFilter);
          });
        }
      }
    ]
  });
}];
