define(function() {
  'use strict';
  return [
    '$scope',
    'Views',
  function($scope, Views) {
    $scope.navbarVars = { read: [ 'tasklistApp' ] };
    $scope.navbarActions = Views.getProviders({ component: 'tasklist.navbar.action' });
  }];
});
