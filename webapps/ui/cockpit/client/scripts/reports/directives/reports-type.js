'use strict';

var fs = require('fs');
var template = fs.readFileSync(__dirname + '/reports-type.html', 'utf8');
var angular = require('camunda-commons-ui/vendor/angular');
var extend = angular.extend;

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
      'search',
      'Views',
    function (
      $scope,
      search,
      Views
    ) {

      var getPluginProviders = $scope.getPluginProviders();

      var updateSilently = function (params) {
        search.updateSilently(params);
      };

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

      $scope.selectReport = function(reportId) {
        var plugin = reportId ? (getPluginProviders({ id: reportId }) || [])[0] : null;
        search.updateSilently({
          report: (plugin || {}).id
        });
        reportsTypeData.set('plugin', plugin);
      };

    }]

  };

}];
