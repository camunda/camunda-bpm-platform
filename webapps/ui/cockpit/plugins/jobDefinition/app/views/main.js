'use strict';

var angular = require('angular'),
    jobDefinitionTable = require('./processDefinition/jobDefinitionTable'),
    jobDefinitionSuspensionState = require('./processDefinition/jobDefinitionSuspensionState'),
    // jobDefinitionSuspensionOverlay = require('./processDefinition/jobDefinitionSuspensionOverlay'),
    suspensionStateAction = require('./processDefinition/suspensionStateAction'),
    diagramPlugins = require('./processDefinition/diagramPlugins');

var ngModule = angular.module('cockpit.plugin.jobDefinition.views', [
  diagramPlugins.name
]);

ngModule.config(jobDefinitionTable);
ngModule.controller('JobDefinitionSuspensionStateController', jobDefinitionSuspensionState);
// ngModule.config(jobDefinitionSuspensionOverlay);
ngModule.config(suspensionStateAction);

module.exports = ngModule;
