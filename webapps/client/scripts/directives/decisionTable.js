/* global define: false */
define([
  'text!./decisionTable.html'
], function(template) {
  'use strict';

  var DirectiveController = ['$scope',
                    function( $scope) {

  }];

  var Directive = function ($compile) {
    return {
      restrict: 'EAC',
      scope: {
        decisionTable: '=',
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
