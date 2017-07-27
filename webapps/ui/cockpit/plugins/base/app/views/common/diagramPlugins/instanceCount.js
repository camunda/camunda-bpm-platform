'use strict';

var fs = require('fs');
var angular = require('angular');

var template = fs.readFileSync(__dirname + '/instanceCount.html', 'utf8');

module.exports = function($scope, control, processData, processDiagram, Loaders, $rootScope, callbacks) {
  var viewer = control.getViewer();
  var overlays = viewer.get('overlays');
  var stopLoading = Loaders.startLoading();
  var overlaysNodes = {};

  callbacks.observe(function(sources) {
    stopLoading();

    Object
      .keys(processDiagram.bpmnElements)
      .forEach(function(key) {
        var element = processDiagram.bpmnElements[key];
        var data = callbacks.getData.apply(null, [element].concat(sources));
        var nodes;

        if (callbacks.isActive(data)) {
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

          element.isSelectable = true;
        }

        if (overlaysNodes[element.id]) {
          callbacks.updateOverlayNodes(overlaysNodes[element.id], data);
        }
      });

    $rootScope.$broadcast('cockpit.plugin.base.views:diagram-plugins:instance-plugin-loaded');
  });

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
};
