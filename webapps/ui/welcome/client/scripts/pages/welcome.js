'use strict';

var fs = require('fs');
var template = fs.readFileSync(__dirname + '/welcome.html', 'utf8');

var RouteConfig = [ '$routeProvider', function($routeProvider) {
  $routeProvider.when('/welcome', {
    template: template,
    // controller: Controller,
    controller: ['$scope', function($scope) {
      var auth = $scope.$root.authentication;

      $scope.canAccessApp = function(appName) {
        return auth.authorizedApps.indexOf(appName) > -1;
      };

      $scope.columnWidth = function() {
        return 12 / (auth.authorizedApps.length - 1);
      };
    }],
    authentication: 'required',
    reloadOnSearch: false
  });
}];

module.exports = RouteConfig;
