ngDefine('cockpit.plugin.base.views', function(module) {

  var Controller = function ($scope, $http, Uri) {

    // input: processInstanceId

    $http.get(Uri.appUri("engine://engine/default/process-instance/" + $scope.processInstanceId + "/variables"), function(response) {
      $scope.variables = response.data;
    });
  };

  Controller.$inject = [ '$scope', '$http', 'Uri' ];

  var Configuration = function PluginConfiguration(ViewsProvider) {

    ViewsProvider.registerDefaultView('cockpit.processInstance.instanceDetails', {
      id: 'variables-tab',
      label: 'Variables',
      url: 'plugin://base/static/app/views/processInstance/variables-tab.html',
      controller: Controller
    });
  };

  Configuration.$inject = ['ViewsProvider'];

  module.config(Configuration);
});
