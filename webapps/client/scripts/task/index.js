'use strict';


define([
  'angular',
  'moment',
  './directives/cam-tasklist-task',
  './directives/cam-tasklist-task-history',
  './directives/cam-tasklist-task-diagram',
  './directives/cam-tasklist-task-meta',
  './directives/cam-tasklist-task-form',
  './modals/cam-tasklist-comment-form',

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
  taskFormDirective,
  taskCommentController
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

  taskModule.controller('commentFormCtrl', taskCommentController);

  taskModule.directive('camTasklistTask', taskDirective);

  taskModule.directive('camTasklistTaskHistory', taskHistoryDirective);

  taskModule.directive('camTasklistTaskDiagram', taskDiagramDirective);

  taskModule.directive('camTasklistTaskMeta', taskMetaDirective);

  taskModule.directive('camTasklistTaskForm', taskFormDirective);

  return taskModule;
});
