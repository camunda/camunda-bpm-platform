  'use strict';
  module.exports = [
    '$scope',
    'Views',
    function($scope, Views) {
      $scope.navbarVars = { read: [ 'tasklistApp' ] };
      $scope.navbarActions = Views.getProviders({ component: 'tasklist.navbar.action' });
    }];
