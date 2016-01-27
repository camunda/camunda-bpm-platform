define(['angular',
  './processDefinition/jobDefinitionData'
], function(angular, jobDefinitionData) {
  var ngModule = angular.module('cockpit.plugin.jobDefinition.data', []);

  ngModule.config(jobDefinitionData);

  return ngModule;
});
