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
    if (newValue == oldValue) {
      return;
    }

    search('page', !newValue || newValue == 1 ? null : newValue);

    updateCallback(newValue, oldValue);
  });

  return pages;
}

function getCurrentPageFromSearch(search) {
  return search().page || 1;
}
