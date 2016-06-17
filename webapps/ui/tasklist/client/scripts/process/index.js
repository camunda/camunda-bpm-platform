'use strict';

var angular = require('camunda-commons-ui/vendor/angular'),
    camNavbarActionStartProcessPlugin = require('./plugins/action/cam-tasklist-navbar-action-start-process-plugin'),
    camProcessStartModalCtrl = require('./plugins/action/modals/cam-tasklist-process-start-modal');

var processModule = angular.module('cam.tasklist.process', [
  'cam.tasklist.client',
  'cam.tasklist.form',
  'ui.bootstrap'
]);


  /* action plugins */
processModule.config(camNavbarActionStartProcessPlugin);

  /* action plugin controller */
processModule.controller('camProcessStartModalCtrl', camProcessStartModalCtrl);

module.exports = processModule;
