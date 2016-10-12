'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/decisionTable.html', 'utf8');

var DirectiveController = ['$scope',
                    function( $scope) {

                      $scope.control = {};

                    }];

var Directive = function() {
  return {
    restrict: 'EAC',
    scope: {
      decisionTable: '=',
      control: '=?',
      table: '@',
      onLoad: '&'
    },
    controller: DirectiveController,
    template: template,

    link: function() {}
  };
};

module.exports = Directive;
