'use strict';

var fs = require('fs');

var angular = require('angular');
var actionTemplate = fs.readFileSync(__dirname + '/suspension-state-action.html', 'utf8');
var dialogTemplate = fs.readFileSync(__dirname + '/job-definition-suspension-state-dialog.html', 'utf8');

var Configuration = function PluginConfiguration(ViewsProvider) {
  ViewsProvider.registerDefaultView('cockpit.jobDefinition.action', {
    id: 'update-suspension-state',
    template: actionTemplate,
    controller: [
      '$scope', '$rootScope', 'search', '$modal',
      function($scope, $rootScope, search, $modal) {

        $scope.openSuspensionStateDialog = function(jobDefinition) {
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

module.exports = Configuration;
