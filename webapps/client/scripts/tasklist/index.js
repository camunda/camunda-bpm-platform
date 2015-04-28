define([
  'angular',

  /* controller */
  './controller/cam-tasklist-list-ctrl',

  /* directives */
  './directives/cam-tasklist-sorting-choices',
  './directives/cam-tasklist-sorting-dropdown',
  './directives/cam-tasklist-sorting-inputs',
  './directives/cam-tasklist-tasks',

  /* filters */
  './filters/cam-query-component',

  /* plugins */
  './plugins/cam-tasklist-search-plugin'

], function(
  angular,

  /* controller */
  camTasklistListCtrl,

  /* directives */
  camTasklistSortingChoices,
  camTasklistSortingDropdown,
  camTasklistSortingInputs,
  camTasklistTasks,

  /* filters */
  camQueryComponent,

  /* plugins */
  camTasklistSearchPlugin

) {
  'use strict';

  var module = angular.module('cam.tasklist.tasklist', [
    'ui.bootstrap'
  ]);

  /* controller */
  module.controller('camListCtrl', camTasklistListCtrl);

  /* directives */
  module.directive('camSortingChoices', camTasklistSortingChoices);
  module.directive('camSortingDropdown', camTasklistSortingDropdown);
  module.directive('camSortingInputs', camTasklistSortingInputs);
  module.directive('camTasks', camTasklistTasks);

  /* filters */
  module.filter('camQueryComponent', camQueryComponent);

  /* plugins */
  module.config(camTasklistSearchPlugin);

  return module;

});
