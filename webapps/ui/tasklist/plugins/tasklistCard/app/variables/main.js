'use strict';
var angular = require('angular');

var camTasklistVariables = require('./directives/cam-tasklist-variables');
var camTasklistVariablesDetailsModalCtrl = require('./modals/cam-tasklist-variables-detail-modal');

var ngModule = angular.module('tasklist.plugin.tasklistCard.variables', [
  'ui.bootstrap',
  'angularMoment'
]);

var tasklistCardVariablesPlugin = ['ViewsProvider', function PluginConfiguration(ViewsProvider) {
  ViewsProvider.registerDefaultView('tasklist.card', {
    id: 'tasklist-card-variables',
    template: '<div cam-tasklist-variables ' +
                'filter-properties="filterProperties" ' +
                'variables="task._embedded.variable" ' +
                'class="row variables"></div>',
    controller: function() {},
    priority: 200
  });
}];


ngModule.config(tasklistCardVariablesPlugin);
ngModule.directive('camTasklistVariables', camTasklistVariables);
ngModule.controller('camTasklistVariablesDetailsModalCtrl', camTasklistVariablesDetailsModalCtrl);

module.exports = ngModule;
