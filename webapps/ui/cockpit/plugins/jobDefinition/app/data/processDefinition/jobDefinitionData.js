'use strict';

var angular = require('angular');

var Controller = [ '$scope', 'processData', 'JobDefinitionResource',
      function($scope, processData, JobDefinitionResource) {

        $scope.$on('$processDefinition.suspensionState.changed', function() {
          processData.changed('jobDefinitions');
        });

        processData.provide('jobDefinitions', ['processDefinition', function(processDefinition) {
          return JobDefinitionResource.query({ processDefinitionId : processDefinition.id }).$promise;
        }]);

        processData.observe(['jobDefinitions', 'bpmnElements'], function(jobDefinitions, bpmnElements) {

          angular.forEach(jobDefinitions, function(jobDefinition) {
            var activityId = jobDefinition.activityId,
                bpmnElement = bpmnElements[activityId];

            jobDefinition.activityName = (bpmnElement && (bpmnElement.name || bpmnElement.id)) || activityId;

          });

        });

      }];

var Configuration = function PluginConfiguration(DataProvider) {

  DataProvider.registerData('cockpit.processDefinition.data', {
    id: 'job-definitions-data',
    controller: Controller
  });
};

Configuration.$inject = ['DataProvider'];

module.exports = Configuration;
