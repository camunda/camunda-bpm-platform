define([
  'text!./cam-tasklist-navigation.html'
], function(
  template
) {
  'use strict';
  return function() {
    return {

      template: template,

      controller: [
        '$scope',
        'Views',
      function($scope, Views) {

        $scope.navbarVars = { read: [ 'tasklistApp' ] };
        $scope.navbarActions = Views.getProviders({ component: 'tasklist.navbar.action' });

      }]
    };
  };
});
