'use strict';

define([
  'angular',

  /* directives */
  './directives/cam-tasklist-sorting-choices',
  './directives/cam-tasklist-search',
  './directives/cam-tasklist-tasks',

  /* filters */
  './filters/cam-query-component',

], function(
  angular,

  /* directives */
  camTasklistSortingChoices,
  camTasklistSearch,
  camTasklistTasks,

  /* filters */
  camQueryComponent

) {
  var module = angular.module('cam.tasklist.tasklist', [
    'ui.bootstrap'
  ]);

  /* directives */
  module.directive('camSortingChoices', camTasklistSortingChoices);
  module.directive('camTaskSearch', camTasklistSearch);
  module.directive('camTasks', camTasklistTasks);

  /* filters */
  module.filter('camQueryComponent', camQueryComponent);

  return module;

});
