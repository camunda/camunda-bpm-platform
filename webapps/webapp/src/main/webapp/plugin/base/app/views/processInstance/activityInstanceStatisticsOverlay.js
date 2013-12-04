ngDefine('cockpit.plugin.base.views', function(module) {

  var Controller = [ '$scope', '$filter', function ($scope, $filter) {

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

  }];

  var Configuration = function PluginConfiguration(ViewsProvider) {

    ViewsProvider.registerDefaultView('cockpit.processInstance.diagram.overlay', {
      id: 'activity-instance-statistics-overlay',
      url: 'plugin://base/static/app/views/processInstance/activity-instance-statistics-overlay.html',
      controller: Controller,
      priority: 20
    }); 
  };

  Configuration.$inject = ['ViewsProvider'];

  module.config(Configuration);
});
