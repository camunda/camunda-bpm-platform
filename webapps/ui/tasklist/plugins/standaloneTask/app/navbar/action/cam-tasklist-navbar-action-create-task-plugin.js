'use strict';

var fs = require('fs');

var createTaskActionTemplate = fs.readFileSync(__dirname + '/cam-tasklist-navbar-action-create-task-plugin.html', 'utf8');
var createTaskModalTemplate = fs.readFileSync(__dirname + '/modals/cam-tasklist-create-task-modal.html', 'utf8');

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

  module.exports = Configuration;
