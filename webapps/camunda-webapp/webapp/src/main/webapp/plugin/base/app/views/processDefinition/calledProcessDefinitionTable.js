ngDefine('cockpit.plugin.base.views', function(module) {

  var Controller = [ '$scope', '$location', '$q', 'PluginProcessDefinitionResource', 
             function($scope, $location, $q, PluginProcessDefinitionResource) {

    var filter;
    var processData = $scope.processData.newChild($scope);

    processData.provide('calledProcessDefinitions', [ 'processDefinition', 'filter', 'bpmnElements', function(processDefinition, newFilter, bpmnElements) {

      filter = angular.copy(newFilter);

      delete filter.page;
      delete filter.scrollToBpmnElement;

      // the parent process definition id is the super process definition id...
      filter.superProcessDefinitionId = filter.parentProcessDefinitionId;
      // ...and the process definition id of the current view is the
      // parent process definition id of query.
      filter.parentProcessDefinitionId = $scope.processDefinition.id;

      filter.activityIdIn = filter.activityIds;
      delete filter.activityIds;

      return PluginProcessDefinitionResource.getCalledProcessDefinitions({ id: processDefinition.id }, filter).$promise;
    }]);

    processData.observe([ 'calledProcessDefinitions', 'bpmnElements' ], function(calledProcessDefinitions, bpmnElements) {

      $scope.calledProcessDefinitions = attachCalledFromActivities(calledProcessDefinitions, bpmnElements);
    });

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
  }];

  var Configuration = [ 'ViewsProvider', function(ViewsProvider) {

    ViewsProvider.registerDefaultView('cockpit.processDefinition.runtime.tab', {
      id: 'call-process-definitions-table',
      label: 'Called Process Definitions',
      url: 'plugin://base/static/app/views/processDefinition/called-process-definition-table.html',
      controller: Controller,
      priority: 5
    });
  }];

  module.config(Configuration);
});
