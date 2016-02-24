'use strict';

var angular = require('camunda-bpm-sdk-js/vendor/angular'),
    dataDepend = require('angular-data-depend'),
    camCommons = require('camunda-commons-ui/lib/index'),


    routes = require('./config/routes'),


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
