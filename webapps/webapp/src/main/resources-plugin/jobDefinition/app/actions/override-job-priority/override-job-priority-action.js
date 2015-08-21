/* global define: false, angular: false */
define(['angular', 'text!./override-job-priority-action.html', 'text!./override-job-priority-dialog.html'], function(angular, actionTemplate, dialogTemplate) {
  'use strict';

  var Configuration = function PluginConfiguration(ViewsProvider) {
    ViewsProvider.registerDefaultView('cockpit.jobDefinition.action', {
      id: 'job-definition-override-job-priority-action',
      template: actionTemplate,
      controller: [
              '$scope', '$rootScope', '$modal',
      function($scope,   $rootScope,   $modal) {

        $scope.openDialog = function (jobDefinition) {
          var dialog = $modal.open({
            resolve: {
              jobDefinition: function() { return jobDefinition; }
            },
            controller: 'JobDefinitionOverrideJobPriorityController',
            template: dialogTemplate
          });

          dialog.result.then(function(result) {
            // dialog closed. YEA!
            if (result.status === 'SUCCESS') {
              $scope.processData.changed('jobDefinitions');
              $scope.processData.set('filter', angular.extend({}, $scope.filter));
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
