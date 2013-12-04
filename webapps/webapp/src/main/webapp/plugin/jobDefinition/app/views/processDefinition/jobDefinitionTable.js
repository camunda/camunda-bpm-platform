ngDefine('cockpit.plugin.jobDefinition.views', ['require'], function(module, require) {

  var Controller = [ '$scope', '$rootScope', '$dialog', 
      function ($scope, $rootScope, $dialog) {

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

    $scope.openSuspensionStateDialog = function (jobDefinition) {
      var dialog = $dialog.dialog({
        resolve: {
          jobDefinition: function() { return jobDefinition; }
        },
        controller: 'JobDefinitionSuspensionStateController',
        templateUrl: require.toUrl('./job-definition-suspension-state-dialog.html')
      });

      dialog.open().then(function(result) {
        // dialog closed. YEA!
        if (result.status === 'SUCCESS') {
          if (result.executeImmediately) {
            jobDefinition.suspended = result.suspended;
            $rootScope.$broadcast('$jobDefinition.suspensionState.changed', $scope.jobDefinition);
          }

          $scope.processData.set('filter', angular.extend({}, $scope.filter));
        }
      });

    };

  }];

  var Configuration = function PluginConfiguration(ViewsProvider) {

    ViewsProvider.registerDefaultView('cockpit.processDefinition.view', {
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
