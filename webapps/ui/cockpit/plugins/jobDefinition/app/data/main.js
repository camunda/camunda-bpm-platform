'use strict';

var angular = require('angular'),
    jobDefinitionData = require('./processDefinition/jobDefinitionData');

var ngModule = angular.module('cockpit.plugin.jobDefinition.data', []);

ngModule.config(jobDefinitionData);

module.exports = ngModule;
