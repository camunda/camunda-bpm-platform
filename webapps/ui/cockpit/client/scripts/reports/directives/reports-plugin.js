'use strict';

var fs = require('fs');
var template = fs.readFileSync(__dirname + '/reports-plugin.html', 'utf8');

module.exports = [function() {

  return {

    restrict: 'A',
    scope: {
      reportData: '='
    },

    template: template,

    controller: [
      '$scope',
      function(
      $scope
    ) {

        var reportPluginData = $scope.reportPluginData = $scope.reportData.newChild($scope);

        reportPluginData.observe('plugin', function(plugin) {
          $scope.plugin = plugin;
        });

        $scope.reportPluginVars = { read: [ 'reportPluginData' ] };

      }]

  };

}];
