'use strict';

var angular = require('angular'),
    tasklistSortingPlugin = require('./tasklist-sorting');



var ngModule = angular.module('tasklist.plugin.tasklistSorting.tasklistHeader', []);

ngModule.config(tasklistSortingPlugin);

module.exports = ngModule;
