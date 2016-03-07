'use strict';

var angular = require('camunda-commons-ui/vendor/angular'),
    reportsView = require('./controllers/reports-view-ctrl'),
    reportsPlugin = require('./directives/reports-plugin'),
    reportsType = require('./directives/reports-type');

var reportsModule = angular.module('cam.cockpit.reports', []);

reportsModule.config(reportsView);

/* directives */
reportsModule.directive('camReportsPlugin', reportsPlugin);
reportsModule.directive('camReportsType', reportsType);

module.exports = reportsModule;
