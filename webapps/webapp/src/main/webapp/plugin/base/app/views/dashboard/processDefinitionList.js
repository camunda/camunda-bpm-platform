ngDefine('cockpit.plugin.base.views', [
  'angular'
], function(module, angular) {

  var Controller = [ '$scope', 'ProcessDefinitionResource', function($scope, ProcessDefinitionResource) {

    var processData = $scope.processData.newChild($scope);

    $scope.orderByPredicate = 'definition.name';
    $scope.orderByReverse = false;

    processData.observe('processDefinitionStatistics', function (processDefinitionStatistics) {
      $scope.statistics = processDefinitionStatistics;
    });

  }];

  var PluginConfiguration = [ 'ViewsProvider', function PluginConfiguration(ViewsProvider) {
    ViewsProvider.registerDefaultView('cockpit.dashboard', {
      id: 'process-definition-list',
      label: 'Deployed Processes',
      url: 'plugin://base/static/app/views/dashboard/process-definition-list.html',
      controller: Controller,
      priority: 5
    });
  }];

  module.config(PluginConfiguration);

  return module;

});
