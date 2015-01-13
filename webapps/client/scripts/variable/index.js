'use strict';



/**
 * @module  cam.tasklist.variables
 * @belongsto cam.tasklist
 *
 * Set of features to deal with variables (from tasks, processes, ...)
 */



define([
  'angular',
  './directives/cam-tasklist-variables',
  './modals/cam-tasklist-variables-detail-modal',
  'angular-moment'
], function(
  angular,
  camTasklistVariables,
  camTasklistVariablesDetailsModalCtrl
) {

  var variableModule = angular.module('cam.tasklist.variables', [
    'ui.bootstrap',
    'angularMoment'
  ]);

  variableModule.directive('camTasklistVariables', camTasklistVariables);
  variableModule.controller('camTasklistVariablesDetailsModalCtrl', camTasklistVariablesDetailsModalCtrl);

  return variableModule;
});
