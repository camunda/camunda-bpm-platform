/* global ngDefine: false */
ngDefine('cockpit.plugin.base.views', function(module) {
  'use strict';

  var Configuration = function PluginConfiguration(ViewsProvider) {

    ViewsProvider.registerDefaultView('cockpit.processInstance.diagram.overlay', {
      id: 'activity-instance-statistics-overlay',
      url: 'plugin://base/static/app/views/processInstance/activity-instance-statistics-overlay.html',
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

  module.config(Configuration);
});
