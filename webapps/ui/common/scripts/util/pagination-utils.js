'use strict';

var angular = require('camunda-commons-ui/vendor/angular');

var DEFAULT_PAGES = { size: 50, total: 0, current: 1 };

module.exports = {
  initializePaginationInController: initializePaginationInController
};

/**
 * Initializes pagination in controller.
 *
 * @param $scope
 * @param search service from controller
 * @param updateCallback callback function that is called each time pagination changes,
 *                       takes two argument newPage and oldPage.
 * @returns {*}
 */
function initializePaginationInController($scope, search, updateCallback) {
  var pages = $scope.pages = angular.copy(DEFAULT_PAGES);
  pages.current = getCurrentPageFromSearch(search);

  $scope.$watch('pages.current', function(newValue, oldValue) {
    // Used for checking if current page change is due to $locationChangeSuccess event
    // If so this change was already passed to updateCallback, so it can be ignored
    var searchCurrentPage = getCurrentPageFromSearch(search);

    if (newValue == oldValue || newValue === searchCurrentPage) {
      return;
    }

    search('page', !newValue || newValue == 1 ? null : newValue);

    updateCallback(newValue, oldValue);
  });

  $scope.$on('$locationChangeSuccess', function() {
    var currentPage = getCurrentPageFromSearch(search);

    if (+pages.current !== +currentPage) {
      var oldCurrent = pages.current;

      pages.current = currentPage;

      updateCallback(pages.current, oldCurrent);
    }
  });

  return pages;
}

function getCurrentPageFromSearch(search) {
  return search().page || 1;
}
