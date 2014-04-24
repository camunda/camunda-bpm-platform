/* global ngDefine: false, angular: false */
ngDefine('cockpit.plugin.base.views', ['require'], function(module, require) {
  'use strict';

  var Configuration = function PluginConfiguration(ViewsProvider) {
    ViewsProvider.registerDefaultView('cockpit.processInstance.runtime.action', {
      id: 'update-suspension-state-action',
      label: 'Update Suspension State',
      url: 'plugin://base/static/app/views/processInstance/update-suspension-state-action.html',
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
            templateUrl: require.toUrl('./update-suspension-state-dialog.html')
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

  module.config(Configuration);

});
