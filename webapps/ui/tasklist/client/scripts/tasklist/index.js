'use strict';

var angular = require('angular'),

  /* controller */
  camTasklistListCtrl = require('./controller/cam-tasklist-list-ctrl'),

  /* directives */
  camTasklistSortingChoices = require('./directives/cam-tasklist-sorting-choices'),
  camTasklistSortingDropdown = require('./directives/cam-tasklist-sorting-dropdown'),
  camTasklistSortingInputs = require('./directives/cam-tasklist-sorting-inputs'),
  camTasklistTasks = require('./directives/cam-tasklist-tasks'),

  /* filters */
  camQueryComponent = require('./filters/cam-query-component'),

  /* plugins */
  camTasklistSearchPlugin = require('./plugins/cam-tasklist-search-plugin');

  var ngModule = angular.module('cam.tasklist.tasklist', [
    'ui.bootstrap'
  ]);

  /* controller */
  ngModule.controller('camListCtrl', camTasklistListCtrl);

  /* directives */
  ngModule.directive('camSortingChoices', camTasklistSortingChoices);
  ngModule.directive('camSortingDropdown', camTasklistSortingDropdown);
  ngModule.directive('camSortingInputs', camTasklistSortingInputs);
  ngModule.directive('camTasks', camTasklistTasks);

  /* filters */
  ngModule.filter('camQueryComponent', camQueryComponent);

  /* plugins */
  ngModule.config(camTasklistSearchPlugin);

  module.exports = ngModule;
