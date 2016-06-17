'use strict';

var angular = require('angular'),
    viewsModule = require('./views/main'),
    dataModule = require('./data/main'),
    actionsModule = require('./actions/main');

module.exports = angular.module('cockpit.plugin.jobDefinition', [viewsModule.name, dataModule.name, actionsModule.name]);
