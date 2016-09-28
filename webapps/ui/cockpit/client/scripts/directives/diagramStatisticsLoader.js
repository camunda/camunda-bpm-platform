'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/diagram-statistics-loader.html', 'utf8');

module.exports = ['Loaders', function(Loaders) {
  return {
    restrict: 'A',
    template: template,
    scope: true,
    controller: ['$scope', function($scope) {
      $scope.loadingStatus = 'INITIAL';

      Loaders.addStatusListener($scope, function(status) {
        $scope.loadingStatus = status;
      });
    }]
  };
}];
