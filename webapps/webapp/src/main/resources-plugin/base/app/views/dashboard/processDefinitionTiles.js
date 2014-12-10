/* global define: false */
define(['text!./process-definition-tiles.html'], function(template) {
  'use strict';

  return [ 'ViewsProvider', function PluginConfiguration(ViewsProvider) {
    ViewsProvider.registerDefaultView('cockpit.dashboard', {
      id: 'process-definition-tiles',
      label: 'Deployed Processes',
      template: template,
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
});
