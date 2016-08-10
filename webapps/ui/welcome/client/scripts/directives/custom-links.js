'use strict';
/* jshint browserify: true */
var fs = require('fs');
var template = fs.readFileSync(__dirname + '/custom-links.html', 'utf8');

module.exports = ['customLinks', function(customLinks) {
  return {
    restrict: 'A',

    template: template,

    replace: true,

    link: function($scope) {
      $scope.links = customLinks;
    }
  };
}];