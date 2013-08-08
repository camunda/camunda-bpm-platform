ngDefine('cockpit.plugin.base.views', [
  'angular'
], function(module) {

  var Controller = [ '$scope', 'ProcessDefinitionResource', function($scope, ProcessDefinitionResource) {

    $scope.orderByPredicate = 'definition.name';
    $scope.orderByReverse = false;

    ProcessDefinitionResource.queryStatistics({ incidents: true }).$then(function (data) {
      $scope.statistics = aggregateStatistics(data.resource);
    });

    var aggregateStatistics = function(statistics) {
      var statisticsResult = [];
      var result = [];

      angular.forEach(statistics, function (currentStatistic) {
        var statistic = statisticsResult[currentStatistic.definition.key];

        if (!statistic) {
          statistic = angular.copy(currentStatistic);
          if (!statistic.definition.name) {
            statistic.definition.name = statistic.definition.key;
          }
          statisticsResult[statistic.definition.key] = statistic;
          result.push(statistic);

        } else {
          // First save the values of instances
          var currentInstances = statistic.instances;
          var currentIncidents = angular.copy(statistic.incidents);

          if (currentStatistic.definition.version > statistic.definition.version) {
            angular.copy(currentStatistic, statistic);
            if (!statistic.definition.name) {
              statistic.definition.name = statistic.definition.key;
            }
          }

          // Add the saved values to the corresponding values of the current statistic
          statistic.instances = currentInstances + currentStatistic.instances;

          angular.forEach(currentIncidents, function (incident) {
            var incidentType = incident.incidentType;
            var incidentCount = incident.incidentCount;

            var newIncident = true;
            for(var i = 0; i < statistic.incidents.length; i++) {
              var statisticIncident = statistic.incidents[i];
              if (statisticIncident.incidentType == incidentType) {
                statisticIncident.incidentCount = incidentCount + statisticIncident.incidentCount;
                newIncident = false;
              }
            }

            if (!!newIncident) {
              // merge the incidents
              statistic.incidents.push(incident);
            }

          });
        }
      });

      return result;
    };

    $scope.shortcutProcessDefinitionName = function (processDefinitionName) {
      return processDefinitionName.substring(0, 25) + '...';
    };

    $scope.isProcessDefinitionNameLong = function (processDefinitionName) {
      if (processDefinitionName.length > 25) {
        return true;
      }
      return false;
    };
  }];


  var PluginConfiguration = [ 'ViewsProvider', function PluginConfiguration(ViewsProvider) {

    ViewsProvider.registerDefaultView('cockpit.dashboard', {
      id: 'process-definition-tiles',
      label: 'Deployed Processes',
      url: 'plugin://base/static/app/views/dashboard/process-definition-tiles.html',
      controller: Controller,
      priority: 0
    });
  }];

  module.config(PluginConfiguration);

  return module;

});
