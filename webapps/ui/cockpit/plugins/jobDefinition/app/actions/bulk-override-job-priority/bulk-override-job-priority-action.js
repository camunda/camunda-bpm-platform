'use strict';

var fs = require('fs');

var angular = require('angular');
var actionTemplate = fs.readFileSync(__dirname + '/bulk-override-job-priority-action.html', 'utf8');
var dialogTemplate = fs.readFileSync(__dirname + '/bulk-override-job-priority-dialog.html', 'utf8');

var Configuration = function PluginConfiguration(ViewsProvider) {
  ViewsProvider.registerDefaultView('cockpit.processDefinition.runtime.action', {
    id: 'bulk-job-definition-override-job-priority-action',
    template: actionTemplate,
    controller: [
      '$scope', '$rootScope', '$modal',
      function($scope,   $rootScope,   $modal) {

        var processData = $scope.processData.newChild($scope);

        var jobDefinitions;
        processData.observe('jobDefinitions', function(_jobDefinitions) {
          jobDefinitions = _jobDefinitions;
        });

        $scope.openDialog = function() {
          var dialog = $modal.open({
            resolve: {
              jobDefinitions: function() { return jobDefinitions; }
            },
            controller: 'BulkJobDefinitionOverrideJobPriorityController',
            template: dialogTemplate
          });

          dialog.result.then(function(result) {
            // dialog closed. YEA!
            if (result.status === 'FINISHED') {
              processData.changed('jobDefinitions');
              processData.set('filter', angular.extend({}, $scope.filter));
            }
          });
        };
      }],
    priority: 10
  });
};

Configuration.$inject = ['ViewsProvider'];

module.exports = Configuration;
