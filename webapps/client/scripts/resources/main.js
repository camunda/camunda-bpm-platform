/* global ngDefine: false */
ngDefine('cockpit.resources', [
  'camunda-commons-ui/util/index',
  './processDefinitionResource',
  './incidentResource',
  './processInstanceResource',
  './localExecutionVariableResource',
  './jobResource',
  './taskResource',
  './jobDefinitionResource'
], function() {});
