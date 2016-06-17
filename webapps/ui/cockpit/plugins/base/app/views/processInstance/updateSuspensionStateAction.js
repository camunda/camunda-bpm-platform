'use strict';

var fs = require('fs');

var actionTemplate = fs.readFileSync(__dirname + '/update-suspension-state-action.html', 'utf8');
var dialogTemplate = fs.readFileSync(__dirname + '/update-suspension-state-dialog.html', 'utf8');
var angular = require('angular');

var Configuration = function PluginConfiguration(ViewsProvider) {
  ViewsProvider.registerDefaultView('cockpit.processInstance.runtime.action', {
    id: 'update-suspension-state-action',
    label: 'Update Suspension State',
    template:actionTemplate,
    controller: [
      '$scope', '$rootScope', '$modal',
      function($scope,   $rootScope,   $modal) {

        $scope.openDialog = function() {
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

module.exports = Configuration;
