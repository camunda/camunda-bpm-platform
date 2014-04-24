/* global ngDefine: false, angular: false */
ngDefine('cockpit.plugin.jobDefinition.views', ['require'], function(module, require) {
  'use strict';

  var Configuration = function PluginConfiguration(ViewsProvider) {
    ViewsProvider.registerDefaultView('cockpit.jobDefinition.action', {
      id: 'update-suspension-state',
      url: 'plugin://jobDefinition/static/app/views/processDefinition/suspension-state-action.html',
      controller: [
              '$scope', '$rootScope', 'search', '$modal',
      function($scope, $rootScope, search, $modal) {

        $scope.openSuspensionStateDialog = function (jobDefinition) {
          var dialog = $modal.open({
            resolve: {
              jobDefinition: function() { return jobDefinition; }
            },
            controller: 'JobDefinitionSuspensionStateController',
            templateUrl: require.toUrl('./job-definition-suspension-state-dialog.html')
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

  module.config(Configuration);
});
