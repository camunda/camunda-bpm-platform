'use strict';

angular.module('dashboard', [])

.config(['$routeProvider', function ($routeProvider) {
  $routeProvider.when('/dashboard', {
    templateUrl:'app/dashboard/dashboard.html',
    controller: 'DashboardCtrl'
  });
}])

.controller('DashboardCtrl', ['$scope', '$location', 'ProcessDefinition', function ($scope, $location, ProcessDefinition) {

  var stubProcessDefinition = [{"id": "1", "name": "Order Process", "key": "order_process_key", "version": 1, "instances": 5, "failedJobs": null},
   {"id": "2", "name": "Order Process", "key": "order_process_key", "version": 2, "instances": 10, "failedJobs": null},
   {"id": "3", "name": "Order Process", "key": "order_process_key", "version": 3, "instances": 2, "failedJobs": null},
   
   {"id": "4", "name": "invoice receipt", "key": "fox_invoice", "version": 1, "instances": 5, "failedJobs": null},
   {"id": "5", "name": "invoice receipt (fox)", "key": "fox_invoice", "version": 2, "instances": 3, "failedJobs": null},
   
   {"id": "6", "name": "Kreditantrag", "key": "kreditantrag_process", "version": 1, "instances": 4, "failedJobs": null},
   {"id": "7", "name": "Kreditantrag", "key": "kreditantrag_process", "version": 2, "instances": 8, "failedJobs": null},
   {"id": "8", "name": null, "key": "kreditantrag_process", "version": 3, "instances": 2, "failedJobs": null}];

  $scope.orderByPredicate = 'name';
  $scope.orderByReverse = false;
  
  ProcessDefinition.queryStatistics(function(data){
//    $scope.processDefinitonStatisticsResults = getStatisticsResult(data);
    $scope.processDefinitions = getStatisticsResult(stubProcessDefinition);
  });
  
  var getStatisticsResult = function(processDefinitions) {
    var statisticsResult = [];
    var result = [];
    
    angular.forEach(processDefinitions, function(processDefinition) {
      var statistics = statisticsResult[processDefinition.key];
      if (!statistics) {
        statistics = {};
        copyStatistics(statistics, processDefinition);
        statistics.instances = processDefinition.instances;
        statisticsResult[processDefinition.key] = statistics;
        result.push(statistics);
      } else {
        statistics.instances += processDefinition.instances;
        if (processDefinition.version > statistics.version) {
          copyStatistics(statistics, processDefinition);
        }
      }
    });
    
    return result;
  };
  
  var copyStatistics = function(source, target) {
    source.id = target.id;
    source.key = target.key;
    source.version = target.version;
    
    if (!!target.name) {
      source.name = target.name;
    } else {
      source.name = target.key;
    }    
  };
  
  $scope.processDefinitionSelected = function (path) {
    $location.path(path);
  };
  
  $scope.extractProcessDefinitionName = function (processDefinition) {
    var name = processDefinition.name;
    if (name.length > 25) {
      return name.substring(0, 25) + "...";
    }
    return name;
  };
  
}]);