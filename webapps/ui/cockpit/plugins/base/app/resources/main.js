'use strict';

var angular = require('angular'),
    processDefinition = require('./processDefinition'),
    processInstance = require('./processInstance');

var ngModule = angular.module('cockpit.plugin.base.resources', []);

ngModule.factory('PluginProcessDefinitionResource', processDefinition);
ngModule.factory('PluginProcessInstanceResource', processInstance);

module.exports = ngModule;
