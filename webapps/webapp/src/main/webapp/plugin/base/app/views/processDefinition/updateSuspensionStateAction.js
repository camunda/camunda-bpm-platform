/* global ngDefine: false, angular: false */
ngDefine('cockpit.plugin.base.views', ['require'], function(module, require) {
  'use strict';
  var Configuration = ['ViewsProvider', function(ViewsProvider) {
    ViewsProvider.registerDefaultView('cockpit.processDefinition.runtime.action', {
      id: 'update-suspension-state-action',
      label: 'Update Suspension State',
      url: 'plugin://base/static/app/views/processDefinition/update-suspension-state-action.html',
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
            templateUrl: require.toUrl('./update-suspension-state-dialog.html')
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

  module.config(Configuration);

});
