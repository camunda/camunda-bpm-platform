/* global ngDefine: false */
ngDefine('cockpit.plugin.base.views', ['angular'], function(module) {
  'use strict';

  var PluginConfiguration = [ 'ViewsProvider', function PluginConfiguration(ViewsProvider) {
    ViewsProvider.registerDefaultView('cockpit.dashboard', {
      id: 'process-definition-tiles',
      label: 'Deployed Processes',
      url: 'plugin://base/static/app/views/dashboard/process-definition-tiles.html',
      controller: [
              '$scope',
      function($scope) {

        var processData = $scope.processData.newChild($scope);

        $scope.orderByPredicate = 'definition.name';
        $scope.orderByReverse = false;

        processData.observe('processDefinitionStatistics', function (processDefinitionStatistics) {
          $scope.statistics = processDefinitionStatistics;

        });

        $scope.shortcutProcessDefinitionName = function (processDefinitionName) {
          return processDefinitionName.substring(0, 25) + '...';
        };

        $scope.isProcessDefinitionNameLong = function (processDefinitionName) {
          if (processDefinitionName.length > 25) {
            return true;
          }
          return false;
        };
      }],
      priority: 0
    });
  }];

  module.config(PluginConfiguration);

  return module;

});
