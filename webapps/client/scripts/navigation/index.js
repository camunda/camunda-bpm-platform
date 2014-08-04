'use strict';

define([
  'angular',
  './directives/cam-tasklist-navigation',
  './directives/cam-sorting-choices',
  'camunda-tasklist-ui/utils',
  'camunda-commons-ui/util/index',
], function(
  angular,
  camTasklistNavigation,
  camSortingChoices
) {
  var navigationModule = angular.module('cam.tasklist.navigation', [
    require('camunda-tasklist-ui/utils').name,
    require('camunda-commons-ui/util/index').name,
    'ui.bootstrap',
    'cam.tasklist.user'
  ]);

  navigationModule.directive('camTasklistNavigation', camTasklistNavigation);
  navigationModule.directive('camSortingChoices', camSortingChoices);

  return navigationModule;
});
