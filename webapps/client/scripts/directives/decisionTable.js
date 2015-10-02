/* global define: false */
define([
  'text!./decisionTable.html'
], function(template) {
  'use strict';

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

  return Directive;
});
