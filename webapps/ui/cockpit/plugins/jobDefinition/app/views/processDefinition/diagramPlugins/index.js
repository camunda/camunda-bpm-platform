'use strict';

var angular = require('angular');
var jobSuspension = require('./jobSuspension');

var ngModule = angular.module('cockpit.plugin.jobDefinition.views.diagramPlugins', []);

ngModule.config(jobSuspension);

module.exports = ngModule;
