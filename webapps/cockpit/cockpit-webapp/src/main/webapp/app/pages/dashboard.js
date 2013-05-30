'use strict';

define(['angular'], function(angular) {

  var module = angular.module('cockpit.pages');

  var Controller = function ($scope, ProcessDefinitionResource, Views) {

    $scope.orderByPredicate = 'definition.name';
    $scope.orderByReverse = false;

    $scope.dashboardProvider = Views.getProvider({ component: 'cockpit.dashboard'});

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

  Controller.$inject = ['$scope', 'ProcessDefinitionResource', 'Views'];

  var RouteConfig = function($routeProvider) {
    $routeProvider.when('/dashboard', {
      templateUrl: 'pages/dashboard.html',
      controller: 'DashboardCtrl'
    });
  };

  RouteConfig.$inject = ['$routeProvider'];

  module
    .config(RouteConfig)
    .controller('DashboardCtrl', Controller);

});
