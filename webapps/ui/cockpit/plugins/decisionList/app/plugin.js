/**
 * @namespace cam.cockpit.plugin
 */

/**
 * @namespace cam.cockpit.plugin.decisionList
 */
'use strict';

var angular = require('angular'),
    viewsModule = require('./views/main');

module.exports = angular.module('cockpit.plugin.decisionList', [viewsModule.name]);
