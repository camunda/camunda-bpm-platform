define([
  'angular',

    /* action plugins */
  './plugins/action/cam-tasklist-navbar-action-start-process-plugin',

    /* action plugin controller */
  './plugins/action/modals/cam-tasklist-process-start-modal'

], function(
  angular,

  /* action plugins */
  camNavbarActionStartProcessPlugin,

  /* action plugin controller */
  camProcessStartModalCtrl


) {

  'use strict';

  var processModule = angular.module('cam.tasklist.process', [
    'cam.tasklist.client',
    'cam.tasklist.form',
    'ui.bootstrap'
  ]);


  /* action plugins */
  processModule.config(camNavbarActionStartProcessPlugin);

  /* action plugin controller */
  processModule.controller('camProcessStartModalCtrl', camProcessStartModalCtrl);

  return processModule;

});
