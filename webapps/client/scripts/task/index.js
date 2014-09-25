'use strict';


define([
  'angular',
  'moment',
  './directives/cam-tasklist-task',
  './directives/cam-tasklist-task-history',
  './directives/cam-tasklist-task-diagram',
  './directives/cam-tasklist-task-meta',
  './directives/cam-tasklist-task-form',

  'camunda-tasklist-ui/utils',
  'camunda-tasklist-ui/api',
  'angular-bootstrap'
], function(
  angular,
  moment,
  taskDirective,
  taskHistoryDirective,
  taskDiagramDirective,
  taskMetaDirective,
  taskFormDirective
) {

  var taskModule = angular.module('cam.tasklist.task', [
    require('camunda-tasklist-ui/utils').name,
    require('camunda-tasklist-ui/api').name,
    'ui.bootstrap',
    'cam.form',
    'angularMoment'
  ]);

  /**
   * @module cam.tasklist.task
   */

  /**
   * @memberof cam.tasklist
   */

  taskModule.directive('camTasklistTask', taskDirective);

  taskModule.directive('camTasklistTaskHistory', taskHistoryDirective);

  taskModule.directive('camTasklistTaskDiagram', taskDiagramDirective);

  taskModule.directive('camTasklistTaskMeta', taskMetaDirective);

  taskModule.directive('camTasklistTaskForm', taskFormDirective);

  return taskModule;
});
