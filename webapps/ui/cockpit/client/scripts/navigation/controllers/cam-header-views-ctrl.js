'use strict';

var angular = require('camunda-commons-ui/vendor/angular');

function checkActive(plugin, path) {
  return path.indexOf(plugin.id) > -1;
}

module.exports = [
  '$scope',
  '$injector',
  '$location',
  'Views',
  function($scope, $injector, $location, Views) {
    $scope.navbarVars = { read: [] };

    $scope.menuActions = [];
    $scope.dropdownActions = [];

    Views.getProviders({ component: 'cockpit.navigation' }).forEach(function(plugin) {
      if (angular.isArray(plugin.access)) {
        var fn = $injector.invoke(plugin.access);

        fn(function(err, access) {
          if (err) { throw err; }

          plugin.accessible = access;
        });
      }

      // accessible by default in case there's no callback
      else {
        plugin.accessible = true;
      }

      (plugin.priority >= 0 ? $scope.menuActions : $scope.dropdownActions).push(plugin);
    });

    $scope.activeClass = function(plugin) {
      var path = $location.absUrl();
      return (typeof plugin.checkActive === 'function' ?
                plugin.checkActive(path) :
                checkActive(plugin, path)) ? 'active' : '';
    };
  }];
