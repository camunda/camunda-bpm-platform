define([
  'angular'
], function(
  angular
) {
  'use strict';
  var $ = angular.element;
  var $bdy = $('body');

  // listen to all click events on the body
  // so the off-canvas navigation (filters)
  // get hidden unless the default behavior
  // is triggered (which could be the click
  // on the button openning the nav)
  $bdy.click(function(evt) {
    // QUESTION: should the target of click event should
    // be taken into account (like when a click is made from
    // inside the navigation)?
    if ($bdy.hasClass('filters-column-open') && !evt.isDefaultPrevented()) {
      $bdy.removeClass('filters-column-open');
    }
  });


  return [
    '$scope',
  function(
    $scope
  ) {
    $scope.toggleFilters = function($event) {
      if ($event && $event.preventDefault) {
        $event.preventDefault();
      }

      $bdy.toggleClass('filters-column-open');
    };
  }];
});
