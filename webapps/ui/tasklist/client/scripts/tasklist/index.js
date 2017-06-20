'use strict';

var angular = require('camunda-commons-ui/vendor/angular'),

  /* controller */
    camTasklistListCtrl = require('./controller/cam-tasklist-list-ctrl'),

  /* directives */
    camTasklistTasks = require('./directives/cam-tasklist-tasks'),

  /* plugins */
    camTasklistSearchPlugin = require('./plugins/cam-tasklist-search-plugin');

var ngModule = angular.module('cam.tasklist.tasklist', [
  'ui.bootstrap'
]);

  /* controller */
ngModule.controller('camListCtrl', camTasklistListCtrl);

  /* directives */
ngModule.directive('camTasks', camTasklistTasks);

  /* plugins */
ngModule.config(camTasklistSearchPlugin);

module.exports = ngModule;
