'use strict';
var fs = require('fs');

var template = fs.readFileSync(__dirname + '/cam-tasklist-task-detail-description-plugin.html', 'utf8');

var Controller = [
  '$scope',
  function() {

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

module.exports = Configuration;
