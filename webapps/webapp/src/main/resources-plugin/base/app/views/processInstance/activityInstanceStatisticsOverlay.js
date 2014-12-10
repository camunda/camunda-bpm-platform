/* global define: false */
define(['text!./activity-instance-statistics-overlay.html'], function(template) {
  'use strict';

  var Configuration = function PluginConfiguration(ViewsProvider) {

    ViewsProvider.registerDefaultView('cockpit.processInstance.diagram.overlay', {
      id: 'activity-instance-statistics-overlay',
      template: template,
      controller: [
               '$scope',
      function ($scope) {
        var bpmnElement = $scope.bpmnElement,
            processData = $scope.processData.newChild($scope);

        $scope.activityInstanceStatistics = processData.observe([ 'activityIdToInstancesMap', 'activityIdToIncidentsMap'],
          function(activityIdToInstancesMap, activityIdToIncidentsMap) {

            var activityId = bpmnElement.id,
                instances = activityIdToInstancesMap[activityId] || [],
                incidents = activityIdToIncidentsMap[activityId] || [];

            if (instances.length || incidents.length) {
              bpmnElement.isSelectable = true;
            }

            $scope.activityInstanceStatistics = { instances: instances, incidents: incidents };
        });

      }],
      priority: 20
    });
  };

  Configuration.$inject = ['ViewsProvider'];

  return Configuration;
});
