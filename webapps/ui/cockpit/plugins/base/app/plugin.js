/**
 * @namespace cam.cockpit.plugin
 */

/**
 * @namespace cam.cockpit.plugin.base
 */
'use strict';

var angular = require('angular'),
    viewsModule = require('./views/main'),
    resourcesModule = require('./resources/main'),
    dataModule = require('./data/main');

module.exports = angular.module('cockpit.plugin.base', [viewsModule.name, resourcesModule.name, dataModule.name]);
