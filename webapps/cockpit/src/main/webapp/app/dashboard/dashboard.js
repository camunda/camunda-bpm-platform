'use strict';

angular.module('dashboard', [])

.config(['$routeProvider', function ($routeProvider) {
  $routeProvider.when('/dashboard', {
    templateUrl:'app/dashboard/dashboard.html',
    controller: 'DashboardCtrl'
  });
}])

.controller('DashboardCtrl', ['$scope', 'ProcessDefinitionResource', function ($scope, ProcessDefinitionResource) {

  $scope.orderByPredicate = 'definition.name';
  $scope.orderByReverse = false;

  ProcessDefinitionResource.queryStatistics(function(data){
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
//        var instances = statistic.instances + currentStatistic.instances;
        if (currentStatistic.definition.version > statistic.definition.version) {
          angular.copy(currentStatistic, statistic);
          if (!statistic.definition.name) {
            statistic.definition.name = statistic.definition.key;
          }
        }
//        statistic.instances = instances;
      }
    });
    
    return result;
  };

  $scope.shortcutProcessDefinitionName = function (processDefinitionName) {
    return processDefinitionName.substring(0, 25) + "...";
  };
  
  $scope.isProcessDefinitionNameLong = function (processDefinitionName) {
    if (processDefinitionName.length > 25) {
      return true;
    }
    return false;
  };
  
}]);