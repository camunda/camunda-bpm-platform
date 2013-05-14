ngDefine('cockpit.plugin.base.pages', function(module) {

  var Controller = function($scope, PluginProcessDefinitionResource) {

    PluginProcessDefinitionResource.query(null, function(data) {
      $scope.processDefinitions = data;
    });

  };

  Controller.$inject = ["$scope", "PluginProcessDefinitionResource"];


  var PluginConfiguration = function PluginConfiguration(PluginsProvider) {

    PluginsProvider.registerDefaultPlugin('cockpit.dashboard', {
      id: 'process-definitions',
      label: 'Deployed Processes',
      url: 'plugin://base/static/app/pages/dashboard-process-definitions.html',
      controller: Controller
    });
  };

  PluginConfiguration.$inject = ['PluginsProvider'];

  module
    .config(PluginConfiguration);

});
