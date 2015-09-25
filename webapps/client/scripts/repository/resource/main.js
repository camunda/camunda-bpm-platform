define([
  'angular',

  /* directives */
  './directives/cam-cockpit-resource',
  './directives/cam-cockpit-resource-meta',

  /* plugins */
  './plugins/details/cam-cockpit-details-plugin',

], function(
  angular,

  /* directives */
  camCockpitResource,
  camCockpitResourceMeta,

  /* plugins */
  camCockpitDetailsPlugin
) {
  'use strict';

  var resourceModule = angular.module('cam.cockpit.repository.resource', []);

  /* directives */
  resourceModule.directive('camResource', camCockpitResource);
  resourceModule.directive('camResourceMeta', camCockpitResourceMeta);

  resourceModule.config(camCockpitDetailsPlugin);

  return resourceModule;
});
