'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/decisionTable.html', 'utf8');

  var DirectiveController = ['$scope',
                    function( $scope) {

    $scope.control = {};

  }];

  var Directive = function ($compile) {
    return {
      restrict: 'EAC',
      scope: {
        decisionTable: '=',
        control: '=?',
        onLoad: '&'
      },
      controller: DirectiveController,
      template: template,

      link: function($scope) {

      }
    };
  };

  Directive.$inject = [ '$compile'];

  module.exports = Directive;
