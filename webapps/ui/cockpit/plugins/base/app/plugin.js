/* global define: false */

/**
 * @namespace cam.cockpit.plugin
 */

/**
 * @namespace cam.cockpit.plugin.base
 */
define(['angular',
        './views/main',
        './resources/main',
        './data/main'
], function(angular, viewsModule, resourcesModule, dataModule) {
  return angular.module('cockpit.plugin.base', [viewsModule.name, resourcesModule.name, dataModule.name]);
});
