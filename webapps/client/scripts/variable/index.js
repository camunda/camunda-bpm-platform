'use strict';



/**
 * @module  cam.tasklist.variables
 * @belongsto cam.tasklist
 *
 * Set of features to deal with variables (from tasks, processes, ...)
 */



define([
  'require',
  'angular',
  'moment',
  './directives/cam-tasklist-variables'
], function(
  require,
  angular,
  moment,
  camTasklistVariables
) {

  var variableModule = angular.module('cam.tasklist.variables', [
    'ui.bootstrap',
    'angularMoment'
  ]);

  variableModule.directive('camTasklistVariables', camTasklistVariables);

  return variableModule;
});
