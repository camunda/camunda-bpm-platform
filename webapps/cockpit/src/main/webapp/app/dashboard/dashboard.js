'use strict';

angular.module('dashboard', [])

.config(['$routeProvider', function ($routeProvider) {
  $routeProvider.when('/dashboard', {
    templateUrl:'app/dashboard/dashboard.html',
    controller: 'DashboardCtrl'
  });
}])

.controller('DashboardCtrl', ['$scope', '$location', 'ProcessDefinition', function ($scope, $location, ProcessDefinitionResource) {

  var stubProcessDefinition = [{"id": "1", "name": "Order Process", "key": "order_process_key", "version": 1, "instances": 5, "failedJobs": null},
   {"id": "2", "name": "Order Process", "key": "order_process_key", "version": 2, "instances": 10, "failedJobs": null},
   {"id": "3", "name": "Order Process", "key": "order_process_key", "version": 3, "instances": 2, "failedJobs": null},
   
   {"id": "4", "name": "invoice receipt", "key": "fox_invoice", "version": 1, "instances": 5, "failedJobs": null},
   {"id": "5", "name": "invoice receipt (fox)", "key": "fox_invoice", "version": 2, "instances": 3, "failedJobs": null},
   
   {"id": "6", "name": "Loan applicant", "key": "loan_applicant_process", "version": 1, "instances": 4, "failedJobs": null},
   {"id": "7", "name": "Loan applicant", "key": "loan_applicant_process", "version": 2, "instances": 8, "failedJobs": null},
   {"id": "8", "name": null, "key": "loan_applicant_process", "version": 3, "instances": 2, "failedJobs": null},
   
   {"id": "9", "name": "Loan applicant, with a very long process definition name", "key": "loan_applicant_process_long_name", "version": 1, "instances": 100, "failedJobs": null},
   
   {"id": "10", "name": "Order Process, the second one", "key": "order_process_key_1", "version": 1, "instances": 5, "failedJobs": null},
   {"id": "11", "name": "Order Process, the second one", "key": "order_process_key_1", "version": 2, "instances": 10, "failedJobs": null},
   {"id": "12", "name": "Order Process, the second one", "key": "order_process_key_1", "version": 3, "instances": 2, "failedJobs": null},
  
   {"id": "13", "name": "invoice receipt new", "key": "fox_invoice_1", "version": 1, "instances": 5, "failedJobs": null},
   {"id": "14", "name": "invoice receipt (fox) new", "key": "fox_invoice_1", "version": 2, "instances": 3, "failedJobs": null},
  
   {"id": "15", "name": "Loan applicant 2", "key": "loan_applicant_process_1", "version": 1, "instances": 4, "failedJobs": null},
   {"id": "16", "name": "Loan applicant 2", "key": "loan_applicant_process_1", "version": 2, "instances": 8, "failedJobs": null},
   {"id": "17", "name": null, "key": "loan_applicant_process_1", "version": 3, "instances": 2, "failedJobs": null},
  
   {"id": "18", "name": "Loan applicant, with a very long process definition name 1", "key": "loan_applicant_process_long_name_1", "version": 1, "instances": 100, "failedJobs": null}];
  
  $scope.orderByPredicate = 'name';
  $scope.orderByReverse = false;

    ProcessDefinitionResource.queryStatistics(function(data){
//    $scope.processDefinitonStatisticsResults = getStatisticsResult(data);
    $scope.processDefinitions = getStatisticsResult(stubProcessDefinition);
  });
  
  var getStatisticsResult = function(processDefinitions) {
    var statisticsResult = [];
    var result = [];
    
    angular.forEach(processDefinitions, function(processDefinition) {
      var statistics = statisticsResult[processDefinition.key];
      if (!statistics) {
        statistics = angular.copy(processDefinition);
        if (!statistics.name) {
          statistics.name = statistics.key;
        }
        statisticsResult[processDefinition.key] = statistics;
        result.push(statistics);
      } else {
        var instances = statistics.instances + processDefinition.instances;
        
        if (processDefinition.version > statistics.version) {
          angular.copy(processDefinition, statistics);
          if (!statistics.name) {
            statistics.name = statistics.key;
          }
        }
        
        statistics.instances = instances;
      }
    });
    
    return result;
  };

  $scope.selectProcessDefinition = function (path) {
    $location.path(path);
  };
  
  $scope.extractProcessDefinitionName = function (processDefinition) {
    var name = processDefinition.name;
    return name.substring(0, 25) + "...";
  };
  
  $scope.isLongProcessDefinitionName = function (processDefinition) {
    if (processDefinition.name.length > 25) {
      return true;
    }
    return false;
  };
  
}]);