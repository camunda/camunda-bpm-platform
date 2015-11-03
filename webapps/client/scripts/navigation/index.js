define([
  'angular',
  './controllers/cam-layout-ctrl',
  './controllers/cam-header-views-ctrl',
  'camunda-commons-ui/util/index'
], function(
  angular,
  camLayoutCtrl,
  camHeaderViewsCtrl
) {
  'use strict';

  var navigationModule = angular.module('cam.tasklist.navigation', [
    require('camunda-commons-ui/util/index').name,
    'ui.bootstrap',
    'cam.tasklist.user'
  ]);

  navigationModule.controller('camHeaderViewsCtrl', camHeaderViewsCtrl);
  navigationModule.controller('camLayoutCtrl', camLayoutCtrl);

  return navigationModule;
});
