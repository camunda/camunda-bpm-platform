'use strict';


define([
  'angular',
  'moment',
  'camunda-tasklist-ui/task/directives/cam-tasklist-task',
  'camunda-tasklist-ui/task/directives/cam-tasklist-task-history',
  'camunda-tasklist-ui/task/directives/cam-tasklist-task-meta',
  'camunda-tasklist-ui/task/directives/cam-tasklist-task-form',

  'camunda-tasklist-ui/utils',
  'camunda-tasklist-ui/api',
  'angular-bootstrap'
], function(
  angular,
  moment,
  taskDirective,
  taskHistoryDirective,
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

  taskModule.directive('camTasklistTaskMeta', taskMetaDirective);

  taskModule.directive('camTasklistTaskForm', taskFormDirective);

  return taskModule;
});
