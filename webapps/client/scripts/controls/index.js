'use strict';
if (typeof define !== 'function') { var define = require('amdefine')(module); }
/* jshint unused: false */
define([
           'angular'
], function(angular) {

  /**
   * @module cam.tasklist.controls
   */

  /**
   * @memberof cam.tasklist
   */

  var controlsModule = angular.module('cam.tasklist.controls', [
  ]);

  controlsModule.directive('camTasklistControls', [
          '$modal', '$rootScope',
  function($modal,   $rootScope) {
    return {
      link: function(scope) {
        scope.batchOpen = false;
        scope.filtersOpen = false;
        $rootScope.batchActions = $rootScope.batchActions || {};
        $rootScope.batchActions.selected = $rootScope.batchActions.selected || [];

        $rootScope.$on('batchaction.selection.changed', function() {
          scope.batchOpen = !!$rootScope.batchActions.selected.length;
        });
      },
      templateUrl: 'scripts/controls/controls.html'
    };
  }]);

  return controlsModule;
});
