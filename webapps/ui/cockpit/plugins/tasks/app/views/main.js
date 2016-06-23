'use strict';

var angular = require('angular'),

    // dashboard
    taskDashboard = require('./dashboard/task-dashboard'),

    ngModule = angular.module('cockpit.plugin.tasks.views', []);

ngModule.config(taskDashboard);

module.exports = ngModule;
