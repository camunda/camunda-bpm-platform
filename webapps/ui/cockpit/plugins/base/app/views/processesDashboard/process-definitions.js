'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/process-definitions.html', 'utf8');

module.exports = [ 'ViewsProvider', function(ViewsProvider) {
  ViewsProvider.registerDefaultView('cockpit.processes.dashboard', {
    id: 'process-definition',
    label: 'Deployed Process Definitions',
    template: template,
    controller: [
      '$scope',
      'Views',
      'camAPI',
      function($scope, Views, camAPI) {
        var processInstancePlugins = Views.getProviders({ component: 'cockpit.processInstance.view' });
        $scope.hasHistoryPlugin = processInstancePlugins.filter(function(plugin) {
          return plugin.id === 'history';
        }).length > 0;

        var processData = $scope.processData.newChild($scope);

        $scope.hasReportPlugin = Views.getProviders({ component: 'cockpit.report' }).length > 0;

        var processDefinitionService = camAPI.resource('process-definition');
        $scope.loadingState = 'LOADING';

        processDefinitionService.list({
          latest: true
        }, function(err, data) {
          $scope.processDefinitionData = data.items;

          if (err) {
            $scope.loadingState = 'ERROR';
          }

          $scope.loadingState = 'LOADED';

          processData.observe('processDefinitionStatistics', function(processDefinitionStatistics) {
            $scope.statistics = processDefinitionStatistics;

            $scope.statistics.forEach(function(statistic) {
              var processDefId = statistic.definition.id;
              var foundIds = $scope.processDefinitionData.filter(function(pd) {
                return pd.id === processDefId;
              });

              var foundObject = foundIds[0];
              if(foundObject) {
                foundObject.incidents = statistic.incidents;
                foundObject.instances = statistic.instances;
              }
            });
          });
        });

        $scope.activeTab = 'list';
        $scope.selectTab = function(tab) {
          $scope.activeTab = tab;
        };
      }],

    priority: 0
  });
}];
