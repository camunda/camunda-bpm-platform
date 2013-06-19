ngDefine('cockpit.plugin.base.views', function(module) {

  var Controller = function ($scope, $http, Uri) {

    // input: processInstanceId

    $http.get(Uri.appUri("engine://process-instance/" + $scope.processInstanceId + "/variables"))
      .success(function(data) {
        var x = [];
        for( var i in data) {
          var tmp = {};
          tmp.name = i;
          tmp.value = data[i].value;
          tmp.type = data[i].type;
          x.push(tmp);
        }
        $scope.variables = x;
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
