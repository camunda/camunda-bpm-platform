define([
  'text!./cam-tasklist-task-detail-description-plugin.html',
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
      id: 'task-detail-description',
      label: 'DESCRIPTION',
      template: template,
      controller: Controller,
      priority: 100
    });
  };

  Configuration.$inject = ['ViewsProvider'];

  return Configuration;

});
