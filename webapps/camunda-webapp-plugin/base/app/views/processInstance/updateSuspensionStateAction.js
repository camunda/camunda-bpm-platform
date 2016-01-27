/* global define: false, angular: false */
define(['angular', 'text!./update-suspension-state-action.html', 'text!./update-suspension-state-dialog.html'], function(angular, actionTemplate, dialogTemplate) {
  'use strict';

  var Configuration = function PluginConfiguration(ViewsProvider) {
    ViewsProvider.registerDefaultView('cockpit.processInstance.runtime.action', {
      id: 'update-suspension-state-action',
      label: 'Update Suspension State',
      template:actionTemplate,
      controller: [
          '$scope', '$rootScope', '$modal',
      function($scope,   $rootScope,   $modal) {

        $scope.openDialog = function () {
          var dialog = $modal.open({
            resolve: {
              processData: function() { return $scope.processData; },
              processInstance: function() { return $scope.processInstance; }
            },
            controller: 'UpdateProcessInstanceSuspensionStateController',
            template: dialogTemplate
          });

          dialog.result.then(function(result) {

            // dialog closed. YEA!
            if (result.status === 'SUCCESS') {
              $scope.processInstance.suspended = result.suspended;
              $rootScope.$broadcast('$processInstance.suspensionState.changed', $scope.processInstance);

              $scope.processData.set('filter', angular.extend({}, $scope.filter));
            }

          });
        };

      }],
      priority: 5
    });
  };

  Configuration.$inject = ['ViewsProvider'];

  return Configuration;

});
