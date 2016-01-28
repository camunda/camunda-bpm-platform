/* global define: false */

/**
 * @namespace cam.cockpit.plugin
 */

/**
 * @namespace cam.cockpit.plugin.decisionList
 */
define(['angular',
        './views/main'
], function(angular, viewsModule) {
  'use strict';

  return angular.module('cockpit.plugin.decisionList', [viewsModule.name]);
});
