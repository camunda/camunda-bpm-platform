/* global define: false, angular: false */
define(['angular', 'text!./suspension-state-action.html', 'text!./job-definition-suspension-state-dialog.html'], function(angular, actionTemplate, dialogTemplate) {
  'use strict';

  var Configuration = function PluginConfiguration(ViewsProvider) {
    ViewsProvider.registerDefaultView('cockpit.jobDefinition.action', {
      id: 'update-suspension-state',
      template: actionTemplate,
      controller: [
              '$scope', '$rootScope', 'search', '$modal',
      function($scope, $rootScope, search, $modal) {

        $scope.openSuspensionStateDialog = function (jobDefinition) {
          var dialog = $modal.open({
            resolve: {
              jobDefinition: function() { return jobDefinition; }
            },
            controller: 'JobDefinitionSuspensionStateController',
            template: dialogTemplate
          });

          dialog.result.then(function(result) {
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

      }],
      priority: 50
    });
  };

  Configuration.$inject = ['ViewsProvider'];

  return Configuration;
});
