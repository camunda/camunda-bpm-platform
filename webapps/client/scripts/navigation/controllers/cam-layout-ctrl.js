define([
  'angular'
], function(
  angular
) {
  'use strict';
  var $ = angular.element;
  var $bdy = $('body');

  function navsOpen() {
    return $bdy.hasClass('filters-column-open') ||
           $bdy.hasClass('list-column-open');
  }

  function isNavChild(el) {
    return $(el).parents('.task-filters, .task-list').length > 0;
  }


  $bdy.click(function(evt) {
    if (navsOpen() &&
        !isNavChild(evt.target) &&
        !evt.isDefaultPrevented()) {
      $bdy.removeClass('filters-column-open list-column-open');
    }
  });


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
