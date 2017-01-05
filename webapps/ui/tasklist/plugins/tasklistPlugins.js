'use strict';

var angular = require('angular'),
    standaloneTask = require('./standaloneTask/app/plugin'),
    tasklistCard = require('./tasklistCard/app/plugin'),
    tasklistSorting = require('./tasklistSorting/app/plugin');

module.exports = angular.module('tasklist.plugin.tasklistPlugins', [standaloneTask.name, tasklistCard.name, tasklistSorting.name]);
