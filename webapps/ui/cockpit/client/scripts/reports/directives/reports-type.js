'use strict';

var fs = require('fs');
var template = fs.readFileSync(__dirname + '/reports-type.html', 'utf8');

module.exports = [function() {

  return {

    restrict: 'A',
    scope: {
      reportData: '=',
      getPluginProviders: '&'
    },

    template: template,

    controller: [
      '$scope',
      '$route',
      function(
      $scope,
      $route
    ) {
        var getPluginProviders = $scope.getPluginProviders();

        var reportsTypeData = $scope.reportsTypeData = $scope.reportData.newChild($scope);

        reportsTypeData.observe('plugin', function(plugin) {
          $scope.plugin = plugin;
          $scope.selection = {
            type: (plugin || {}).id
          };
        });

        reportsTypeData.observe('plugins', function(plugins) {
          $scope.plugins = plugins;
        });

        if ($route.current.params.reportType) {
          var plugin = (getPluginProviders({ id: $route.current.params.reportType }) || [])[0];
          reportsTypeData.set('plugin', plugin);
        }
      }]
  };

}];
