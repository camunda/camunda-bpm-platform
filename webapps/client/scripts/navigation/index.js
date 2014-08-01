'use strict';

define([
  'angular',
  './directives/cam-tasklist-navigation',
  'camunda-tasklist-ui/utils',
  'camunda-commons-ui/util/index',
], function(
  angular,
  camTasklistNavigation
) {
  var navigationModule = angular.module('cam.tasklist.navigation', [
    require('camunda-tasklist-ui/utils').name,
    require('camunda-commons-ui/util/index').name,
    'ui.bootstrap',
    'cam.tasklist.user'
  ]);

  navigationModule.directive('camTasklistNavigation', camTasklistNavigation);

  return navigationModule;
});
