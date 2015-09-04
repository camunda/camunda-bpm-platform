/* global define: false */
define([
  'angular',
  'jquery',
  'text!./decisionTable.html'
], function(angular, $, template) {
  'use strict';
  /* jshint unused: false */
  var _unique = 0;
  function unique(prefix) {
    _unique++;
    return (prefix ? prefix +'_' : '') + _unique;
  }

  var DirectiveController = ['$scope', '$compile', 'Views', '$timeout',
                    function( $scope,   $compile,   Views,   $timeout) {

    $scope.$watch('decisionTable', function(newValue) {
      if(newValue) {
        // render the dmn table
        // console.log('new value for decisionTable', newValue);
      }
    });

  }];

  var Directive = function ($compile, Views) {
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

  Directive.$inject = [ '$compile', 'Views'];

  return Directive;
});
