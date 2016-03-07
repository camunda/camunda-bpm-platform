/**
 * @module  cam.tasklist.variables
 * @belongsto cam.tasklist
 *
 * Set of features to deal with variables (from tasks, processes, ...)
 */

'use strict';

var angular = require('camunda-commons-ui/vendor/angular'),
    camTasklistVariables = require('./directives/cam-tasklist-variables'),
    camTasklistVariablesDetailsModalCtrl = require('./modals/cam-tasklist-variables-detail-modal');

require('angular-moment');

  var variableModule = angular.module('cam.tasklist.variables', [
    'ui.bootstrap',
    'angularMoment'
  ]);

  variableModule.directive('camTasklistVariables', camTasklistVariables);
  variableModule.controller('camTasklistVariablesDetailsModalCtrl', camTasklistVariablesDetailsModalCtrl);

  module.exports = variableModule;
