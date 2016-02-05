'use strict';

module.exports = [
  '$scope',
  '$location',
  'Views',
function($scope, $location, Views) {
  $scope.navbarVars = { read: [] };
  $scope.navbarActions = Views.getProviders({ component: 'cockpit.navbar.action' });

  $scope.activeClass = function(link) {
    var path = $location.absUrl();
    return path.indexOf(link) != -1 ? "active" : "";
  };
}];
