define([
  'text!./process-definitions.html'
], function(
  template
) {
  'use strict';

  return [ 'ViewsProvider', function (ViewsProvider) {
    ViewsProvider.registerDefaultView('cockpit.dashboard', {
      id: 'process-definition',
      label: 'Deployed Process Definitions',
      dashboardMenuLabel: 'BPMN Workflows',
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

        $scope.selected = 'list';
        $scope.selectTab = function (which) {
          $scope.selected = which;
        };
      }],

      priority: 0
    });
  }];
});
