/* global define: false, angular: false */
define(['angular', 'text!./bulk-override-job-priority-action.html', 'text!./bulk-override-job-priority-dialog.html'], function(angular, actionTemplate, dialogTemplate) {
  'use strict';

  var Configuration = function PluginConfiguration(ViewsProvider) {
    ViewsProvider.registerDefaultView('cockpit.processDefinition.runtime.action', {
      id: 'bulk-job-definition-override-job-priority-action',
      template: actionTemplate,
      controller: [
              '$scope', '$rootScope', '$modal',
      function($scope,   $rootScope,   $modal) {

        var processData = $scope.processData.newChild($scope);

        var jobDefinitions;
        processData.observe('jobDefinitions', function(_jobDefinitions) {
          jobDefinitions = _jobDefinitions;
        });

        $scope.openDialog = function () {
          var dialog = $modal.open({
            resolve: {
              jobDefinitions: function() { return jobDefinitions; }
            },
            controller: 'BulkJobDefinitionOverrideJobPriorityController',
            template: dialogTemplate
          });

          dialog.result.then(function(result) {
            // dialog closed. YEA!
            if (result.status === 'FINISHED') {
              processData.changed('jobDefinitions');
              processData.set('filter', angular.extend({}, $scope.filter));
            }
          });
        };
      }],
      priority: 10
    });
  };

  Configuration.$inject = ['ViewsProvider'];

  return Configuration;
});
