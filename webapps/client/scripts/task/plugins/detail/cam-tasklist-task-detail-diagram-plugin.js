define([
  'text!./cam-tasklist-task-detail-diagram-plugin.html',
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
      id: 'task-detail-diagram',
      label: 'DIAGRAM',
      template: template,
      controller: Controller,
      priority: 600
    });
  };

  Configuration.$inject = ['ViewsProvider'];

  return Configuration;

});
