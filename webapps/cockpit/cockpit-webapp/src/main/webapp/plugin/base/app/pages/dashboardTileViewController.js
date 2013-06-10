ngDefine('cockpit.plugin.base.pages', function(module) {

  var Controller = function($scope, ProcessDefinitionResource) {

    $scope.orderByPredicate = 'definition.name';
    $scope.orderByReverse = false;
    
    ProcessDefinitionResource.queryStatistics()
    .$then(function (data) {
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
          if (currentStatistic.definition.version > statistic.definition.version) {
            angular.copy(currentStatistic, statistic);
            if (!statistic.definition.name) {
              statistic.definition.name = statistic.definition.key;
            }
          }
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
    
  };

  Controller.$inject = ["$scope", "ProcessDefinitionResource"];
  

  var PluginConfiguration = function PluginConfiguration(ViewsProvider) {

    ViewsProvider.registerDefaultView('cockpit.dashboard', {
      id: 'process-definitions',
      label: 'Deployed Processes',
      url: 'plugin://base/static/app/pages/dashboard-tile-view.html',
      controller: Controller,
      priority: 0
    });
  };

  PluginConfiguration.$inject = [ 'ViewsProvider' ];

  module
    .config(PluginConfiguration);

  return module;
  
});
