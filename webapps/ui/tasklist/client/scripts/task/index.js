'use strict';

var angular = require('camunda-commons-ui/vendor/angular'),

    taskDirective = require('./directives/cam-tasklist-task'),
    taskMetaDirective = require('./directives/cam-tasklist-task-meta'),

    camTaskActionCtrl = require('./controller/cam-tasklist-task-action-ctrl'),
    camTaskGroupsCtrl = require('./controller/cam-tasklist-task-groups-ctrl'),

  /* detail plugins */
    camTaskDetailFormPlugin = require('./plugins/detail/cam-tasklist-task-detail-form-plugin'),
    camTaskDetailHistoryPlugin = require('./plugins/detail/cam-tasklist-task-detail-history-plugin'),
    camTaskDetailDiagramPlugin = require('./plugins/detail/cam-tasklist-task-detail-diagram-plugin'),
    camTaskDetailDescriptionPlugin = require('./plugins/detail/cam-tasklist-task-detail-description-plugin'),

  /* action plugins */
    camTaskActionCommentPlugin = require('./plugins/action/cam-tasklist-task-action-comment-plugin'),

  /* action plugin controller */
    camCommentCreateModalCtrl = require('./plugins/action/modals/cam-tasklist-comment-form'),

  /* modals */
    camGroupEditModalCtrl = require('./modals/cam-tasklist-groups-modal'),

  /* API */
    apiClient = require('../api/index');

require('camunda-commons-ui/vendor/ui-bootstrap-tpls-0.11.2-camunda');
require('angular-moment');

var taskModule = angular.module('cam.tasklist.task', [
  apiClient.name,
  'ui.bootstrap',
  'cam.tasklist.form',
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
taskModule.controller('camTaskGroupsCtrl', camTaskGroupsCtrl);

  /* detail plugins */
taskModule.config(camTaskDetailFormPlugin);
taskModule.config(camTaskDetailHistoryPlugin);
taskModule.config(camTaskDetailDiagramPlugin);
taskModule.config(camTaskDetailDescriptionPlugin);

    /* action plugins */
taskModule.config(camTaskActionCommentPlugin);

  /* action plugin controller */
taskModule.controller('camCommentCreateModalCtrl', camCommentCreateModalCtrl);

taskModule.controller('camGroupEditModalCtrl', camGroupEditModalCtrl);

module.exports = taskModule;
