'use strict';

var angular = require('angular'),
    processDefinition = require('./dashboard/processDefinitionStatisticsData'),
    activityInstance = require('./processDefinition/activityInstanceStatisticsData');

var ngModule = angular.module('cockpit.plugin.base.data', []);

ngModule.config(processDefinition);
ngModule.config(activityInstance);

module.exports = ngModule;
