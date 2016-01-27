define([
  'angular',
  './action/cam-tasklist-navbar-action-create-task-plugin',
  './action/modals/cam-tasklist-create-task-modal'
], function(
  angular,
  createTaskPlugin,
  createTaskModal
) {
  var ngModule = angular.module('tasklist.plugin.standaloneTask.navbar.action', []);

  ngModule.config(createTaskPlugin);

  ngModule.controller('camCreateTaskModalCtrl', createTaskModal);

  return ngModule;
});
