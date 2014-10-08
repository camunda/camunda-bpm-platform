'use strict';

define([
  'angular',
  './directives/cam-tasklist-navigation',
  './directives/cam-sorting-choices',
  './directives/cam-task-search',
  './controllers/cam-task-search-ctrl',
  './filters/cam-query-component',
  'camunda-tasklist-ui/utils',
  'camunda-commons-ui/util/index'
], function(
  angular,
  camTasklistNavigation,
  camSortingChoices,
  camTaskSearch,
  taskSearchCtrl,
  camQueryComponentFilter
) {
  var navigationModule = angular.module('cam.tasklist.navigation', [
    require('camunda-tasklist-ui/utils').name,
    require('camunda-commons-ui/util/index').name,
    'ui.bootstrap',
    'cam.tasklist.user'
  ]);

  navigationModule.controller('taskSearchCtrl', taskSearchCtrl);
  navigationModule.directive('camTasklistNavigation', camTasklistNavigation);
  navigationModule.directive('camSortingChoices', camSortingChoices);
  navigationModule.directive('camTaskSearch', camTaskSearch);
  navigationModule.filter('queryComponent', camQueryComponentFilter);

  return navigationModule;
});
