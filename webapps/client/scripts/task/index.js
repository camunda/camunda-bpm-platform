'use strict';


define([
  'angular',
  'moment',

  './directives/cam-tasklist-task',
  './directives/cam-tasklist-task-meta',

  './controller/cam-tasklist-task-action-ctrl',

  /* detail plugins */
  './plugins/detail/cam-tasklist-task-detail-form-plugin',
  './plugins/detail/cam-tasklist-task-detail-history-plugin',
  './plugins/detail/cam-tasklist-task-detail-diagram-plugin',
  './plugins/detail/cam-tasklist-task-detail-description-plugin',

  /* detail plugin directives */
  './plugins/detail/directives/cam-tasklist-task-form',
  './plugins/detail/directives/cam-tasklist-task-diagram',


  /* action plugins */
  './plugins/action/cam-tasklist-task-action-comment-plugin',

  /* action plugin controller */
  './plugins/action/modals/cam-tasklist-comment-form',

  './directives/cam-tasklist-groups-form',
  './filters/cam-groups-list',

  'camunda-tasklist-ui/utils',
  'camunda-tasklist-ui/api',
  'angular-bootstrap'

], function(
  angular,
  moment,

  taskDirective,
  taskMetaDirective,

  camTaskActionCtrl,

  /* detail plugins */
  camTaskDetailFormPlugin,
  camTaskDetailHistoryPlugin,
  camTaskDetailDiagramPlugin,
  camTaskDetailDescriptionPlugin,

  /* detail plugin directives */
  taskFormDirective,
  taskDiagramDirective,

  /* action plugins */
  camTaskActionCommentPlugin,

  /* action plugin controller */
  camCommentCreateModalCtrl,

  camGroupEditModalCtrl,
  camGroupsListFilter
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

  taskModule.directive('camTasklistTaskMeta', taskMetaDirective);

  taskModule.controller('camTaskActionCtrl', camTaskActionCtrl);

  /* detail plugins */
  taskModule.config(camTaskDetailFormPlugin);
  taskModule.config(camTaskDetailHistoryPlugin);
  taskModule.config(camTaskDetailDiagramPlugin);
  taskModule.config(camTaskDetailDescriptionPlugin);

  /* detail plugin directives */
  taskModule.directive('camTasklistTaskForm', taskFormDirective);
  taskModule.directive('camTasklistTaskDiagram', taskDiagramDirective);

    /* action plugins */
  taskModule.config(camTaskActionCommentPlugin);

  /* action plugin controller */
  taskModule.controller('camCommentCreateModalCtrl', camCommentCreateModalCtrl);

  taskModule.controller('camGroupEditModalCtrl', camGroupEditModalCtrl);
  taskModule.filter('groupsList', camGroupsListFilter);

  return taskModule;
});
