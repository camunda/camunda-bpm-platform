ngDefine('cockpit.services', function(module) {
  
  var SearchFactory = [ '$location', '$rootScope', function($location, $rootScope) {

    var silent = false;

    $rootScope.$on('$routeUpdate', function(e, lastRoute) {
      if (silent) {
        console.log('silenced $routeUpdate');
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
      angular.forEach(params, function(value, key) {
        $location.search(key, value);
      });

      silent = true;
    };

    return search;
  }];

  module.factory('search', SearchFactory);
});