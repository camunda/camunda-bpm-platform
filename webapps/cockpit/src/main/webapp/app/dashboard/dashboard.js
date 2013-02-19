'use strict';

angular.module('dashboard', ['cockpit.services'])

.config(['$routeProvider', 'AppProvider', function ($routeProvider, AppProvider) {
  $routeProvider.when(AppProvider.root() + '/dashboard', {
    templateUrl:'app/dashboard/dashboard.html',
    controller:'DashboardCtrl'
  });
}])

.controller('DashboardCtrl', ['$scope', function($scope) {
  var test = null;
}]);