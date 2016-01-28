/* global define: false, angular: false */
define(['angular', 'text!./update-suspension-state-action.html', 'text!./update-suspension-state-dialog.html'], function(angular, actionTemplate, dialogTemplate) {
  'use strict';
  return ['ViewsProvider', function(ViewsProvider) {
    ViewsProvider.registerDefaultView('cockpit.processDefinition.runtime.action', {
      id: 'update-suspension-state-action',
      label: 'Update Suspension State',
      template: actionTemplate,
      controller: [
              '$scope', '$rootScope', '$modal',
      function($scope,   $rootScope,   $modal) {

        $scope.openDialog = function () {
          var dialog = $modal.open({
            resolve: {
              processData: function() { return $scope.processData; },
              processDefinition: function() { return $scope.processDefinition; }
            },
            controller: 'UpdateProcessDefinitionSuspensionStateController',
            template: dialogTemplate
          });

          dialog.result.then(function(result) {

            // dialog closed. YEA!
            if (result.status === 'SUCCESS') {
              if (result.executeImmediately) {
                $scope.processDefinition.suspended = result.suspended;
                $rootScope.$broadcast('$processDefinition.suspensionState.changed', $scope.processDefinition);
              }

              $scope.processData.set('filter', angular.extend({}, $scope.filter));
            }
          });
        };

      }],
      priority: 50
    });
  }];
});
