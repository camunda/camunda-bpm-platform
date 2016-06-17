'use strict';

var angular = require('camunda-commons-ui/vendor/angular'),
    camCockpitResources = require('./directives/cam-cockpit-resources');

var resourcesModule = angular.module('cam.cockpit.repository.resources', []);

  /* directives */
resourcesModule.directive('camResources', camCockpitResources);

module.exports = resourcesModule;
