'use strict';

var angular = require('camunda-commons-ui/vendor/angular'),

  /* controllers */
    camResourceDetailsCtrl = require('./controllers/cam-cockpit-resource-details-ctrl'),

  /* directives */
    camCockpitResourceWrapper = require('./directives/cam-cockpit-resource-wrapper'),
    camCockpitResourceMeta = require('./directives/cam-cockpit-resource-meta'),
    camCockpitResourceContent = require('./directives/cam-cockpit-resource-content'),
    camCockpitSource = require('./directives/cam-cockpit-source'),

  /* plugins */
    camCockpitDefinitionsPlugin = require('./plugins/details/definitions/cam-cockpit-definitions-plugin'),
    camCockpitResourceDownloadPlugin = require('./plugins/actions/download/cam-cockpit-resource-action-download-plugin');

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

module.exports = resourceModule;
