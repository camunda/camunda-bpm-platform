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

var fs = require('fs');

var template = require('./tenants.html')();
var searchConfig = require('./tenants-search-plugin-config.json');

var debouncePromiseFactory = require('camunda-bpm-sdk-js').utils
  .debouncePromiseFactory;
var debounceQuery = debouncePromiseFactory();
var debounceCount = debouncePromiseFactory();

var angular = require('../../../../../camunda-commons-ui/vendor/angular');

var Controller = [
  '$scope',
  '$location',
  'search',
  'TenantResource',
  'camAPI',
  'page',
  '$translate',
  function(
    $scope,
    $location,
    search,
    TenantResource,
    camAPI,
    pageService,
    $translate
  ) {
    $scope.searchConfig = angular.copy(searchConfig);

    $scope.blocked = true;
    $scope.onSearchChange = updateView;

    $scope.query = $scope.pages = null;
    var sorting;

    $scope.onSortInitialized = function(_sorting) {
      sorting = _sorting;
      $scope.blocked = false;
    };

    $scope.onSortChanged = function(_sorting) {
      sorting = _sorting;
      updateView();
    };

    function updateView(query, pages) {
      if (query && pages) {
        $scope.query = query;
        $scope.pages = pages;
      }

      var page = $scope.pages.current,
        count = $scope.pages.size,
        firstResult = (page - 1) * count;

      var queryParams = {
        firstResult: firstResult,
        maxResults: count,
        sortBy: sorting.sortBy,
        sortOrder: sorting.sortOrder
      };

      $scope.tenantList = null;
      $scope.loadingState = 'LOADING';

      return debounceCount(
        TenantResource.count(angular.extend({}, $scope.query)).$promise
      )
        .then(function(data) {
          var total = data.count;

          return debounceQuery(
            TenantResource.query(angular.extend({}, $scope.query, queryParams))
              .$promise
          )
            .then(function(data) {
              $scope.tenantList = data;
              $scope.loadingState = data.length ? 'LOADED' : 'EMPTY';

              return total;
            })
            .catch(angular.noop);
        })
        .catch(angular.noop)
        .finally(function() {
          setTimeout(() => {
            $scope.$apply();
          }, 0);
        });
    }

    $scope.availableOperations = {};
    camAPI.resource('tenant').options(function(err, res) {
      angular.forEach(res.links, function(link) {
        $scope.availableOperations[link.rel] = true;
      });
    });

    $scope.$root.showBreadcrumbs = true;

    pageService.titleSet($translate.instant('TENANTS_TENANTS'));

    pageService.breadcrumbsClear();

    pageService.breadcrumbsAdd({
      label: $translate.instant('TENANTS_TENANTS'),
      href: '#/tenants/'
    });
  }
];

module.exports = [
  '$routeProvider',
  function($routeProvider) {
    $routeProvider.when('/tenants', {
      template: template,
      controller: Controller,
      authentication: 'required',
      reloadOnSearch: false
    });
  }
];
