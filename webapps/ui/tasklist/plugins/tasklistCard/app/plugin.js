'use strict';

var angular = require('angular');
var variablePluginModule = require('./variables/main');

module.exports = angular.module('tasklist.plugin.tasklistCard', [variablePluginModule.name]);
