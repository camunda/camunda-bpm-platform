define([
  'text!./cam-tasklist-task-detail-form-plugin.html',
], function(
  template
) {
  'use strict';

  var Controller = [
   '$scope',
  function ($scope) {

  }];

  var Configuration = function PluginConfiguration(ViewsProvider) {

    ViewsProvider.registerDefaultView('tasklist.task.detail', {
      id: 'task-detail-form',
      label: 'FORM',
      template: template,
      controller: Controller,
      priority: 1000
    });
  };

  Configuration.$inject = ['ViewsProvider'];

  return Configuration;

});
