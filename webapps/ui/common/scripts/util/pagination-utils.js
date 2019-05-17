/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

'use strict';

var angular = require('camunda-commons-ui/vendor/angular');

var DEFAULT_PAGES = {size: 50, total: 0, current: 1};

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
  var pages = ($scope.pages = angular.copy(DEFAULT_PAGES));
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

  $scope.$on('$destroy', function() {
    search('page', null);
  });

  return pages;
}

function getCurrentPageFromSearch(search) {
  return search().page || 1;
}
