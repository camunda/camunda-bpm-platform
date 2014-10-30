define([
  'angular'
], function(
  angular
) {
  'use strict';
  var $ = angular.element;
  var $bdy = $('body');

  return [
    '$scope',
  function(
    $scope
  ) {
    $scope.toggleOffCanvas = function($event) {
      if ($event && $event.preventDefault) {
        $event.preventDefault();
      }
      var target = $($event.currentTarget).attr('data-off-canvas-toggle');
      $bdy.toggleClass(target +'-column-open');
    };
  }];
});
