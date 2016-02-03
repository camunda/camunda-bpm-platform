'use strict';

var angular = require('angular'),
    standaloneTask = require('./standaloneTask/app/plugin');

module.exports = angular.module('tasklist.plugin.tasklistPlugins', [standaloneTask.name]);
