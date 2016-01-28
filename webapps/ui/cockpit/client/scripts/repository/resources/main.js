'use strict';

var angular = require('angular'),
    camCockpitResources = require('./directives/cam-cockpit-resources');

  var resourcesModule = angular.module('cam.cockpit.repository.resources', []);

  /* directives */
  resourcesModule.directive('camResources', camCockpitResources);

  module.exports = resourcesModule;
