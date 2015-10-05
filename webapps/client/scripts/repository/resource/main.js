define([
  'angular',

  /* controllers */
  './controllers/cam-cockpit-resource-details-ctrl',

  /* directives */
  './directives/cam-cockpit-resource-wrapper',
  './directives/cam-cockpit-resource-meta',
  './directives/cam-cockpit-resource-content',
  './directives/cam-cockpit-source',

  /* plugins */
  './plugins/details/definitions/cam-cockpit-definitions-plugin',
  './plugins/actions/download/cam-cockpit-resource-action-download-plugin',

], function(
  angular,

  /* controllers */
  camResourceDetailsCtrl,

  /* directives */
  camCockpitResourceWrapper,
  camCockpitResourceMeta,
  camCockpitResourceContent,
  camCockpitSource,

  /* plugins */
  camCockpitDefinitionsPlugin,
  camCockpitResourceDownloadPlugin
) {
  'use strict';

  var resourceModule = angular.module('cam.cockpit.repository.resource', []);

  /* controllers */
  resourceModule.controller('camResourceDetailsCtrl', camResourceDetailsCtrl);

  /* directives */
  resourceModule.directive('camResourceWrapper', camCockpitResourceWrapper);
  resourceModule.directive('camResourceMeta', camCockpitResourceMeta);
  resourceModule.directive('camResourceContent', camCockpitResourceContent);
  resourceModule.directive('camSource', camCockpitSource);

  /* plugins */
  resourceModule.config(camCockpitDefinitionsPlugin);
  resourceModule.config(camCockpitResourceDownloadPlugin);

  return resourceModule;
});
