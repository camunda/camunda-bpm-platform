define([
  'angular',

  'camunda-commons-ui/util/index',
  './processDefinitionResource',
  './incidentResource',
  './processInstanceResource',
  './localExecutionVariableResource',
  './jobResource',
  './taskResource',
  './jobDefinitionResource'
], function(
  angular,

  commonsUi,
  processDefinitionResource,
  incidentResource,
  processInstanceResource,
  localExecutionVariableResource,
  jobResource,
  taskResource,
  jobDefinitionResource
) {

  'use strict';

  var resourcesModule = angular.module('cam.cockpit.resources', []);

  resourcesModule.factory('ProcessDefinitionResource', processDefinitionResource);
  resourcesModule.factory('IncidentResource', incidentResource);
  resourcesModule.factory('ProcessInstanceResource', processInstanceResource);
  resourcesModule.factory('LocalExecutionVariableResource', localExecutionVariableResource);
  resourcesModule.factory('JobResource', jobResource);
  resourcesModule.factory('TaskResource', taskResource);
  resourcesModule.factory('JobDefinitionResource', jobDefinitionResource);

  return resourcesModule;

});
