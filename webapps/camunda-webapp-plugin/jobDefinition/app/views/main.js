define(['angular',
  './processDefinition/jobDefinitionTable',
  './processDefinition/jobDefinitionSuspensionState',
  './processDefinition/jobDefinitionSuspensionOverlay',
  './processDefinition/suspensionStateAction'
], function(angular,
  jobDefinitionTable,
  jobDefinitionSuspensionState,
  jobDefinitionSuspensionOverlay,
  suspensionStateAction) {
  var ngModule = angular.module('cockpit.plugin.jobDefinition.views', []);

  ngModule.config(jobDefinitionTable);
  ngModule.controller('JobDefinitionSuspensionStateController', jobDefinitionSuspensionState);
  ngModule.config(jobDefinitionSuspensionOverlay);
  ngModule.config(suspensionStateAction);

  return ngModule;
});
