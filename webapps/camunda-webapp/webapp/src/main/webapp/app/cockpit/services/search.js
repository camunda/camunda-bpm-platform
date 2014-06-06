/* global ngDefine: false */
ngDefine('cockpit.services', function(module) {
  'use strict';

  var SearchFactory = [ '$location', '$rootScope', function($location, $rootScope) {

    var silent = false;

    $rootScope.$on('$routeUpdate', function(e, lastRoute) {
      if (silent) {
        silent = false;
      } else {
        $rootScope.$broadcast('$routeChanged', lastRoute);
      }
    });

    $rootScope.$on('$routeChangeSuccess', function(e, lastRoute) {
      silent = false;
    });

    var search = function() {
      var args = Array.prototype.slice(arguments);

      return $location.search.apply($location, arguments);
    }

    search.updateSilently = function(params) {
      var oldPath = $location.absUrl();

      angular.forEach(params, function(value, key) {
        $location.search(key, value);
      });

      var newPath = $location.absUrl();

      if (newPath != oldPath) {
        silent = true;
      }
    };

    return search;
  }];

  module.factory('search', SearchFactory);
});
