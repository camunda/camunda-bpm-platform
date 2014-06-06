ngDefine('cockpit.plugin.jobDefinition.views', ['require'], function(module, require) {

  var Controller = [
    '$scope',
    'Views',
  function ($scope, Views) {

    var processData = $scope.processData.newChild($scope),
        processDefinition = null;

    processData.observe([ 'filter', 'jobDefinitions', 'bpmnElements' ], function(filter, jobDefinitions, bpmnElements) {
      updateView(filter, jobDefinitions, bpmnElements);
    });

    function updateView(filter, jobDefinitions, bpmnElements) {

      $scope.jobDefinitions = null;

      var activityIds = filter.activityIds;

      if (!activityIds || !activityIds.length) {
        $scope.jobDefinitions = jobDefinitions;
        return;
      }

      var jobDefinitionSelection = [];

      angular.forEach(jobDefinitions, function(jobDefinition) {

        var activityId = jobDefinition.activityId;

        if (activityIds.indexOf(activityId) != -1) {
          jobDefinitionSelection.push(jobDefinition);
        }

      });

      $scope.jobDefinitions = jobDefinitionSelection;

    };

    $scope.jobDefinitionVars = { read: [ 'jobDefinition', 'processData', 'filter' ] };
    $scope.jobDefinitionActions = Views.getProviders({ component: 'cockpit.jobDefinition.action' });

  }];

  var Configuration = function PluginConfiguration(ViewsProvider) {

    ViewsProvider.registerDefaultView('cockpit.processDefinition.runtime.tab', {
      id: 'job-definition-table',
      label: 'Job Definitions',
      url: 'plugin://jobDefinition/static/app/views/processDefinition/job-definition-table.html',
      controller: Controller,
      priority: 2
    });
  };

  Configuration.$inject = ['ViewsProvider'];

  module.config(Configuration);
});
