'use strict';

var fs = require('fs');

var angular = require('angular');
var actionTemplate = fs.readFileSync(__dirname + '/incident-external-task-action.html', 'utf8');
var dialogTemplate = fs.readFileSync(__dirname + '/job-retry-dialog.html', 'utf8');

var Configuration = function PluginConfiguration(ViewsProvider) {
  ViewsProvider.registerDefaultView('cockpit.incident.action', {
    id: 'increase-incident-external-task-retry',
    template: actionTemplate,
    controller: [
      '$scope', '$rootScope', 'search', '$modal',
      function($scope, $rootScope, search, $modal) {

        $scope.openExternalTaskRetryDialog = function(incident) {
          var dialog = $modal.open({
            resolve: {
              incident: function() { return incident; }
            },
            controller: 'ExternalTaskRetryController',
            template: dialogTemplate
          });

          dialog.result.then(function(result) {
            if (result === 'finished') {
              // refresh filter and all views
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
