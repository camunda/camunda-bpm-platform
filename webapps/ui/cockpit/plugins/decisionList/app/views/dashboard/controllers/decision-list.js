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

var angular = require('angular');

module.exports = [
  '$scope',
  'decisionList',
  'Views',
  'localConf',
  '$location',
  'search',
  function($scope, decisionList, Views, localConf, $location, search) {
    $scope.loadingState = 'LOADING';
    $scope.drdDashboard = Views.getProvider({
      component: 'cockpit.plugin.drd.dashboard'
    });
    $scope.isDrdDashboardAvailable = !!$scope.drdDashboard;

    var initialSearch = $location.search();

    var pages = ($scope.paginationController = {
      decisionPages: {
        size: 50,
        total: 0,
        current: initialSearch.decisionPage || 1
      },
      drdPages: {size: 50, total: 0, current: initialSearch.drdPage || 1},
      changeDecisionPage: changeDecisionPage,
      changeDecisionSorting: changeDecisionSorting,
      changeDrdPage: changeDrdPage,
      changeDrdSorting: changeDrdSorting
    });

    var decisionSorting = localConf.get('sortDecDefTable', {
      sortBy: 'name',
      sortOrder: 'asc'
    });
    var drdSorting = localConf.get('sortDRDTab', {
      sortBy: 'name',
      sortOrder: 'asc'
    });

    function changeDrdPage(pages) {
      $scope.loadingState = 'LOADING';
      search.updateSilently({drdPage: pages.current});
      $scope.paginationController.drdPages.current = pages.current;
      updateDrdPage();
    }

    function changeDecisionSorting(sorting) {
      decisionSorting = sorting;
      localConf.set('sortDecDefTable', sorting);
      updateDecisionPage();
    }

    function changeDrdSorting(sorting) {
      drdSorting = sorting;
      localConf.set('sortDRDTab', sorting);
      updateDrdPage();
    }

    function changeDecisionPage(pages) {
      $scope.loadingState = 'LOADING';
      search.updateSilently({decisionPage: pages.current});
      $scope.paginationController.decisionPages.current = pages.current;
      updateDecisionPage();
    }

    function updateDrdPage() {
      decisionList
        .getDrds(
          angular.extend(
            {},
            {
              firstResult: (pages.drdPages.current - 1) * pages.drdPages.size,
              maxResults: pages.drdPages.size
            },
            drdSorting
          )
        )
        .then(function(data) {
          $scope.loadingState = 'LOADED';

          $scope.drds = data;
        });
    }

    function updateDecisionPage() {
      decisionList
        .getDecisions(
          angular.extend(
            {},
            {
              firstResult:
                (pages.decisionPages.current - 1) * pages.decisionPages.size,
              maxResults: pages.decisionPages.size
            },
            decisionSorting
          )
        )
        .then(function(data) {
          $scope.loadingState = 'LOADED';

          $scope.decisions = data;
        });
    }

    decisionList
      .getDecisionsLists(
        angular.extend(
          {},
          {
            firstResult:
              (pages.decisionPages.current - 1) * pages.decisionPages.size,
            maxResults: pages.decisionPages.size
          },
          decisionSorting
        ),
        angular.extend(
          {},
          {
            firstResult: (pages.drdPages.current - 1) * pages.drdPages.size,
            maxResults: pages.drdPages.size
          },
          drdSorting
        )
      )
      .then(function(data) {
        $scope.loadingState = 'LOADED';

        pages.decisionPages.total = $scope.decisionCount = data.decisionsCount;
        $scope.decisions = data.decisions;

        pages.drdPages.total = $scope.drdsCount = data.drdsCount;

        $scope.drds = data.drds;
      })
      .catch(function(err) {
        $scope.loadingError = err.message;
        $scope.loadingState = 'ERROR';

        throw err;
      });

    $scope.drdDashboardVars = {
      read: ['drdsCount', 'drds', 'paginationController']
    };
  }
];
