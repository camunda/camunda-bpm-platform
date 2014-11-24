/* global ngDefine: false */
ngDefine('cockpit.plugin.base.views', function(module) {
  'use strict';

  var PluginConfiguration = [ 'ViewsProvider', function PluginConfiguration(ViewsProvider) {
    ViewsProvider.registerDefaultView('cockpit.dashboard', {
      id: 'process-definition-list',
      label: 'Deployed Processes',
      url: 'plugin://base/static/app/views/dashboard/process-definition-list.html',
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

  module.config(PluginConfiguration);

  return module;

});
