ngDefine('cockpit.plugin.base.pages', function(module) {

  var Controller = function($scope, PluginProcessDefinitionResource, ProcessDefinitionResource) {

    ProcessDefinitionResource.queryStatistics({"failedJobs": true}, function(data){
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
            angular.copy(currentStatistic, statistic);
            if (!statistic.definition.name) {
              statistic.definition.name = statistic.definition.key;
            }
            statistic.instances = currentInstances + currentStatistic.instances;
          }
        }
      });
      
      return result;
    };
    
    PluginProcessDefinitionResource.query(null, function(data) {
      $scope.processDefinitions = data;
    });

  };

  Controller.$inject = ["$scope", "PluginProcessDefinitionResource", "ProcessDefinitionResource"];


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
