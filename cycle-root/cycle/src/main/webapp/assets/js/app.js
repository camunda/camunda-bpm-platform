'use strict';

angular
  .module('cycle', ['ng', 'cycle.filters', 'cycle.services', 'cycle.directives'])
    .config(['$routeProvider', '$locationProvider', '$httpProvider', function($routeProvider, $locationProvider, $httpProvider) {
      $routeProvider.when('/', {
    	controller: HomeController,
        templateUrl: '../partials/no-roundtrip-selected.html'
      });
      
      $routeProvider.when('/roundtrip/:roundtripId', { 
        controller: RoundtripDetailsController, 
        templateUrl: '../partials/roundtrip-details.html'
      });

      $httpProvider.defaults.transformRequest.push(function(d) {
        console.log(d);
        return d;
      });
      
      // $routeProvider.otherwise({redirectTo: '/'});

      // $locationProvider.html5Mode(true);
    }]);