'use strict';

var angular = require('camunda-commons-ui/vendor/angular'),
    dashboardLink = require('./plugins/dashboardLink'),
    repositoryLink = require('./plugins/repositoryLink'),
    reportsLink = require('./plugins/reportsLink'),
    camHeaderViewsCtrl = require('./controllers/cam-header-views-ctrl');

var navigationModule = angular.module('cam.cockpit.navigation', []);

navigationModule.controller('camHeaderViewsCtrl', camHeaderViewsCtrl);

navigationModule.config(dashboardLink);
navigationModule.config(repositoryLink);
navigationModule.config(reportsLink);


module.exports = navigationModule;
