'use strict';

var Controller = [ '$scope', 'processData', 'ProcessDefinitionResource',
      function($scope, processData, ProcessDefinitionResource) {

        processData.provide('activityInstanceStatistics', ['processDefinition', function(processDefinition) {
          return ProcessDefinitionResource.queryActivityStatistics({ id : processDefinition.id, incidents: true }).$promise;
        }]);

      }];

var Configuration = function PluginConfiguration(DataProvider) {

  DataProvider.registerData('cockpit.processDefinition.data', {
    id: 'activity-instance-statistics-data',
    controller: Controller
  });
};

Configuration.$inject = ['DataProvider'];

module.exports = Configuration;
