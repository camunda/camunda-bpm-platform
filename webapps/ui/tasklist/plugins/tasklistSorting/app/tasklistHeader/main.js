'use strict';

var angular = require('angular'),
    camTasklistSortingChoices = require('./cam-tasklist-sorting-choices'),
    camTasklistSortingDropdown = require('./cam-tasklist-sorting-dropdown'),
    camTasklistSortingInputs = require('./cam-tasklist-sorting-inputs'),
    tasklistSortingPlugin = require('./tasklist-sorting');




var ngModule = angular.module('tasklist.plugin.tasklistSorting.tasklistHeader', []);
ngModule.directive('camSortingChoices', camTasklistSortingChoices);
ngModule.directive('camSortingDropdown', camTasklistSortingDropdown);
ngModule.directive('camSortingInputs', camTasklistSortingInputs);

ngModule.config(tasklistSortingPlugin);

module.exports = ngModule;
