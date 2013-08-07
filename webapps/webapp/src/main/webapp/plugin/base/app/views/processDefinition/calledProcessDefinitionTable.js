ngDefine('cockpit.plugin.base.views', function(module) {

  var Controller = [ '$scope', '$location', '$q', 'PluginProcessDefinitionResource', 
             function($scope, $location, $q, PluginProcessDefinitionResource) {

    var filter;
    var processData = $scope.processData;

    processData.get([ 'processDefinition', 'filter', 'bpmnElements' ], function(processDefinition, filter, bpmnElements) {

      var parentId = filter.parentProcessDefinitionId,
          activityIds = filter.activityIds;

      return PluginProcessDefinitionResource.getCalledProcessDefinitions({ id: processDefinition.id }, {
        activityIdIn: activityIds,
        superProcessDefinitionId: parentId
      }).$promise.then(function(definitions) {
        $scope.calledProcessDefinitions = attachCalledFromActivities(definitions, bpmnElements);
      });

      // remember last filter
      filter = filter;
    });

    // processData.get([ 'calledProcessDefinitions', 'bpmnElements' ], function(calledProcessDefinitions, bpmnElements) {

    //   $scope.calledProcessDefinitions = attachCalledFromActivities(calledProcessDefinitions, bpmnElements);
    // });

    function attachCalledFromActivities(processDefinitions, bpmnElements) {

      var result = [];

      angular.forEach(processDefinitions, function(d) {
        var calledFromActivityIds = d.calledFromActivityIds,
            calledFromActivities = [];

        angular.forEach(calledFromActivityIds, function(activityId) {
          var bpmnElement = bpmnElements[activityId];
          var activity = { id: activityId, name: bpmnElement.name || activityId };

          calledFromActivities.push(activity);
        });

        result.push(angular.extend({}, d, { calledFromActivities: calledFromActivities }));
      });

      return result;
    }
    
    $scope.selectBpmnElement = function(activityId) {
      var newFilter = angular.extend({}, filter, { activityIds: [ activityId ], scrollToBpmnElement: activityId });

      processData.set('filter', newFilter);
    };
  }];

  var Configuration = function PluginConfiguration(ViewsProvider) {

    ViewsProvider.registerDefaultView('cockpit.processDefinition.view', {
      id: 'call-process-definitions-table',
      label: 'Called Process Definitions',
      url: 'plugin://base/static/app/views/processDefinition/called-process-definition-table.html',
      controller: Controller,
      priority: 5
    });
  };

  Configuration.$inject = ['ViewsProvider'];

  module.config(Configuration);
});
