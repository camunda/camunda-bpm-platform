'use strict';

angular.module('process.definition', [])

.config(['$routeProvider', function ($routeProvider) {
  $routeProvider.when('/process-definition/:processDefinitionId', {
    templateUrl:'app/process-definition/process-definition.html',
    controller: 'ProcessDefinitionCtrl'
  });
}])

.controller('ProcessDefinitionCtrl', ['$scope', 'ProcessDefinition', function($scope, ProcessDefinition) {
  $scope.helloWorld = "Hello World";
  
}]);