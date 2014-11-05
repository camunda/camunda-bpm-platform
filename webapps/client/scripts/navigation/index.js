'use strict';

define([
  'angular',
  './directives/cam-tasklist-navigation',
  './controllers/cam-layout-ctrl',
  'camunda-commons-ui/util/index'
], function(
  angular,
  camTasklistNavigation,
  camLayoutCtrl
) {
  var navigationModule = angular.module('cam.tasklist.navigation', [
    require('camunda-commons-ui/util/index').name,
    'ui.bootstrap',
    'cam.tasklist.user'
  ]);

  navigationModule.controller('camLayoutCtrl', camLayoutCtrl);
  navigationModule.directive('camTasklistNavigation', camTasklistNavigation);

  return navigationModule;
});
