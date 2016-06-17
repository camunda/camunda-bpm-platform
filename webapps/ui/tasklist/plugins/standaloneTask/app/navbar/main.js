'use strict';

var angular = require('angular'),
    createTaskPlugin = require('./action/cam-tasklist-navbar-action-create-task-plugin'),
    createTaskModal = require('./action/modals/cam-tasklist-create-task-modal');

var ngModule = angular.module('tasklist.plugin.standaloneTask.navbar.action', []);

ngModule.config(createTaskPlugin);

ngModule.controller('camCreateTaskModalCtrl', createTaskModal);

module.exports = ngModule;
