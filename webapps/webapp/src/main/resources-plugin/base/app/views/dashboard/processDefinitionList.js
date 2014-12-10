/* global define: false */
define(['text!./process-definition-list.html'], function(template) {
  'use strict';
  return [ 'ViewsProvider', function PluginConfiguration(ViewsProvider) {
    ViewsProvider.registerDefaultView('cockpit.dashboard', {
      id: 'process-definition-list',
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
      }],
      priority: 5
    });
  }];
});
