define([
  'angular',

  /* directives */
  './directives/cam-cockpit-resources'

], function(
  angular,

  /* directives */
  camCockpitResources

) {
  'use strict';

  var resourcesModule = angular.module('cam.cockpit.repository.resources', []);

  /* directives */
  resourcesModule.directive('camResources', camCockpitResources);

  return resourcesModule;
});
