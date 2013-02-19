'use strict';

angular.module('dashboard', ['cockpit.services'])

.config(['$routeProvider', function ($routeProvider) {
  $routeProvider.when('/dashboard', {
    templateUrl:'app/dashboard/dashboard.html',
    controller: 'DashboardCtrl'
  });
}])

.controller('DashboardCtrl', ['$scope', 'ProcessDefinition', function($scope, ProcessDefinition) {
  $scope.helloWorld = "Hello World";
  
  $scope.processDefinitions = ProcessDefinition.query();
  
}]);