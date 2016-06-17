'use strict';

var angular = require('camunda-commons-ui/vendor/angular');

require('angular-data-depend');
require('camunda-commons-ui/lib/index');


var routes = require('./config/routes'),

    camCockpitRepositoryViewCtrl = require('./controllers/cam-cockpit-repository-view-ctrl'),

    deploymentsModule = require('./deployments/main'),
    resourcesModule = require('./resources/main'),
    resourceDetailsModule = require('./resource/main');

var ngDeps = [
  'cam.commons',
  'dataDepend',
  'ngRoute',
  deploymentsModule.name,
  resourcesModule.name,
  resourceDetailsModule.name
];

var deploymentModule = angular.module('cam.cockpit.repository', ngDeps);

deploymentModule.config(routes);

deploymentModule.controller('camCockpitRepositoryViewCtrl', camCockpitRepositoryViewCtrl);

module.exports = deploymentModule;
