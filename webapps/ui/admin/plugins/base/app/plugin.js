/**
 * @namespace cam.admin.plugin
 */

/**
 * @namespace cam.admin.plugin.base
 */
'use strict';

var angular = require('angular'),
    viewsModule = require('./views/main');

module.exports = angular.module('admin.plugin.base', [viewsModule.name]);
