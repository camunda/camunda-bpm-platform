'use strict';

var $ = require('jquery');
var $bdy = $('body');

module.exports = [
  '$scope',
  '$timeout',
  function(
    $scope,
    $timeout
  ) {
    $scope.toggleVariableSearch = function($event) {
      if ($event && $event.preventDefault) {
        $event.preventDefault();
      }

      $('.tasks-list').toggleClass('show-search');
    };



    function region($event) {
      return $($event.currentTarget).attr('data-region');
    }

    function isClosed(target) {
      return $bdy.hasClass(target +'-column-close');
    }

    function open(target) {
      return $bdy.removeClass(target +'-column-close');
    }

    function close(target) {
      return $bdy.addClass(target +'-column-close');
    }

    $scope.toggleRegion = function($event) {
      if ($event && $event.preventDefault) {
        $event.preventDefault();
      }

      var target = region($event);

      // list-column-close is not allowed when task-column-close
      if (target === 'task') {
        if(isClosed('list') && !isClosed('task')) {
          open('list');
        }
      }


      else if (target === 'list') {
        if(isClosed('task') && !isClosed('list')) {
          open('task');
        }
      }

      $bdy.toggleClass(target +'-column-close');
      $timeout(function() {
        $scope.$root.$broadcast('layout:change');
      }, 600);
    };

    $scope.maximizeRegion = function($event) {
      if ($event && $event.preventDefault) {
        $event.preventDefault();
      }

      close('filters');
      close('list');
      open('task');
      document.querySelector('.reset-regions').focus();
    };

    $scope.resetRegions = function($event) {
      if ($event && $event.preventDefault) {
        $event.preventDefault();
      }

      open('filters');
      open('list');
      open('task');
      document.querySelector('.maximize').focus();
    };
  }];
