'use strict';

var angular = require('camunda-bpm-sdk-js/vendor/angular'),
    camCockpitResources = require('./directives/cam-cockpit-resources');

  var resourcesModule = angular.module('cam.cockpit.repository.resources', []);

  /* directives */
  resourcesModule.directive('camResources', camCockpitResources);

  module.exports = resourcesModule;
