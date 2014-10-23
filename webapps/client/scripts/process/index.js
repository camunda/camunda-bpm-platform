define([
  'angular',
  './controller/cam-tasklist-process-start-ctrl',
  './modals/cam-tasklist-process-start-modal-ctrl'
], function(
  angular,
  camProcessStartCtrl,
  camProcessStartModalCtrl
) {

  'use strict';

  var processModule = angular.module('cam.tasklist.process', [
    'cam.tasklist.client',
    'cam.tasklist.form',
    'ui.bootstrap'
  ]);


  processModule.controller('camProcessStartCtrl', camProcessStartCtrl);
  processModule.controller('camProcessStartModalCtrl', camProcessStartModalCtrl);

  return processModule;

});
