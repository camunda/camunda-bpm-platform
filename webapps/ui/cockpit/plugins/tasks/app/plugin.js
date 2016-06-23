/**
 * @namespace cam.cockpit.plugin.tasks
 */
'use strict';

var angular     = require('angular'),
    viewsModule = require('./views/main');

module.exports = angular.module('cockpit.plugin.tasks', [viewsModule.name]);
