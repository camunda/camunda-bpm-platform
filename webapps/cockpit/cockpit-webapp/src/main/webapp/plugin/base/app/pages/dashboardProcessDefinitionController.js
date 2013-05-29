ngDefine('cockpit.plugin.base.pages', function(module) {

  var Controller = function($scope, ProcessDefinitionResource) {

    ProcessDefinitionResource.queryStatistics(
        {"failedJobs": true},
        function(data){
          $scope.statistics = getStatisticsResult(data);
    });

    var getStatisticsResult = function(statistics) {
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
          if (currentStatistic.definition.version > statistic.definition.version) {
            var currentInstances = statistic.instances;
            var currentFailedJobs = statistic.failedJobs;
            angular.copy(currentStatistic, statistic);
            if (!statistic.definition.name) {
              statistic.definition.name = statistic.definition.key;
            }
            statistic.instances = currentInstances + currentStatistic.instances;
            statistic.failedJobs = currentFailedJobs + currentStatistic.failedJobs;
          }
        }
      });

      return result;
    };

  };

  Controller.$inject = ["$scope", "ProcessDefinitionResource"];
  

  var PluginConfiguration = function PluginConfiguration(ViewsProvider) {

    ViewsProvider.registerDefaultView('cockpit.dashboard', {
      id: 'process-definitions',
      label: 'Deployed Processes',
      url: 'plugin://base/static/app/pages/dashboard-process-definitions.html',
      controller: Controller
    });
  };

  PluginConfiguration.$inject = [ 'ViewsProvider' ];

  module
    .config(PluginConfiguration);

  return module;
  
});
