/* global ngDefine: false */
ngDefine('cockpit.resources', [
  'module:camunda.common.services.uri:camunda-common/services/uri',
  './processDefinitionResource',
  './incidentResource',
  './processInstanceResource',
  './localExecutionVariableResource',
  './jobResource',
  './taskResource',
  './jobDefinitionResource'
], function() {});
