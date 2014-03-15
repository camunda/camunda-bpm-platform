ngDefine('cockpit.plugin.base.views', function(module) {

  var Controller = [ '$scope', '$filter', function ($scope, $filter) {

    var bpmnElement = $scope.bpmnElement,
        processData = $scope.processData.newChild($scope);

    processData.provide('activityInstance', ['activityInstanceStatistics', function (activityInstanceStatistics) {
      for (var i = 0; i < activityInstanceStatistics.length; i++) {
        var current = activityInstanceStatistics[i];
        if (current.id === bpmnElement.id) {
          return current;
        }
      }
      return null;
    }]);

    $scope.activityInstance = processData.observe('activityInstance', function(activityInstance) {
      if (activityInstance) {
        bpmnElement.isSelectable = true;
      }

      $scope.activityInstance = activityInstance;
    });

  }];

  var Configuration = [ 'ViewsProvider', function(ViewsProvider) {

    ViewsProvider.registerDefaultView('cockpit.processDefinition.diagram.overlay', {
      id: 'activity-instance-statistics-overlay',
      url: 'plugin://base/static/app/views/processDefinition/activity-instance-statistics-overlay.html',
      controller: Controller,
      priority: 20
    });
  }];

  module.config(Configuration);
});
