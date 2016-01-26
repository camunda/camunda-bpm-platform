define([
  'text!./cam-tasklist-navbar-action-create-task-plugin.html',
  'text!./modals/cam-tasklist-create-task-modal.html'
], function(
  createTaskActionTemplate,
  createTaskModalTemplate
) {
  'use strict';

  var Controller = [
   '$scope',
   '$modal',
  function (
    $scope,
    $modal
  ) {

    $scope.open = function() {
      $modal.open({
        size: 'lg',
        controller: 'camCreateTaskModalCtrl',
        template: createTaskModalTemplate
      }).result.then(function(result) {
        if ($scope.tasklistApp && $scope.tasklistApp.refreshProvider) {
          $scope.tasklistApp.refreshProvider.refreshTaskList();
        }
      });
    };

  }];

  var Configuration = function PluginConfiguration(ViewsProvider) {

    ViewsProvider.registerDefaultView('tasklist.navbar.action', {
      id: 'create-task-action',
      template: createTaskActionTemplate,
      controller: Controller,
      priority: 200
    });
  };

  Configuration.$inject = ['ViewsProvider'];

  return Configuration;

});
