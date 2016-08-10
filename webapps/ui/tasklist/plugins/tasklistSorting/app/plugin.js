'use strict';

var angular = require('angular');
var pluginModule = require('./tasklistHeader/main');

module.exports = angular.module('tasklist.plugin.tasklistSorting', [pluginModule.name]);
