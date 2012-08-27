'use strict';

angular
  .module('cycle', ['ng', 'cycle.filters', 'cycle.services', 'cycle.directives'])
    .config(['$routeProvider', '$locationProvider', '$httpProvider', function($routeProvider, $locationProvider, $httpProvider) {
      $routeProvider.when('/', {
        templateUrl: '../partials/no-roundtrip-selected.html'
      });
      
      $routeProvider.when('/roundtrip/:roundtripId', { 
        controller: RoundtripDetailsController, 
        templateUrl: '../partials/roundtrip-details.html'
      });
      
      // $routeProvider.otherwise({redirectTo: '/'});

      // $locationProvider.html5Mode(true);
    }]);