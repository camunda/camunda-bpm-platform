'use strict';

var angular = require('angular');

var DEFAULT_PAGES = { size: 50, total: 0, current: 1 };

module.exports = {
  initializePaginationInController: initializePaginationInController
};

function initializePaginationInController($scope, search, updateCallback) {
  var pages = $scope.pages = angular.copy(DEFAULT_PAGES);

  pages.current = getCurrentPageFromSearch(search);

  $scope.$watch('pages.current', function(newValue, oldValue) {
    if (newValue == oldValue) {
      return;
    }

    search('page', !newValue || newValue == 1 ? null : newValue);
  });

  $scope.$on('$routeChanged', function() {
    var oldValue = pages.current;
    var newValue = getCurrentPageFromSearch(search);

    pages.current = newValue;

    updateCallback(newValue, oldValue);
  });

  return pages;
}

/**
 * Gets current page from url or returns 1 as default page
 *
 * @param search
 * @returns {*|number}
 */
function getCurrentPageFromSearch(search) {
  return search().page || 1;
}
