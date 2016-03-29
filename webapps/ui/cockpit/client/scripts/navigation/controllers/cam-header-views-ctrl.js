'use strict';

function checkActive(plugin, path) {
  return path.indexOf(plugin.id) > -1;
}

module.exports = [
  '$scope',
  '$location',
  'Views',
function($scope, $location, Views) {
  $scope.navbarVars = { read: [] };
  $scope.navbarActions = Views.getProviders({ component: 'cockpit.dashboard.section' });
  $scope.activeClass = function(plugin) {
    var path = $location.absUrl();
    return (typeof plugin.checkActive === 'function' ?
                plugin.checkActive(path) :
                checkActive(plugin, path)) ? 'active' : '';
  };
}];
