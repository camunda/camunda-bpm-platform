'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/process-definitions.html', 'utf8');

  module.exports = [ 'ViewsProvider', function (ViewsProvider) {
    ViewsProvider.registerDefaultView('cockpit.dashboard', {
      id: 'process-definition',
      label: 'Deployed Process Definitions',
      dashboardMenuLabel: 'BPMN Processes',
      template: template,
      controller: [
              '$scope',
              'Views',
      function($scope, Views) {
        var processInstancePlugins = Views.getProviders({ component: 'cockpit.processInstance.view' });
        $scope.hasHistoryPlugin = processInstancePlugins.filter(function(plugin) {
          return plugin.id === 'history';
        }).length > 0;

        var processData = $scope.processData.newChild($scope);

        $scope.orderByPredicate = 'definition.name';
        $scope.orderByReverse = false;

        $scope.hasReportPlugin = Views.getProviders({ component: 'cockpit.report' }).length > 0;

        processData.observe('processDefinitionStatistics', function (processDefinitionStatistics) {
          $scope.statistics = processDefinitionStatistics;
        });

        $scope.selected = 'list';
        $scope.selectTab = function (which) {
          $scope.selected = which;
        };
      }],

      priority: 0
    });
  }];
