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

var template = require('./cam-cockpit-deployments-sorting-choices.html')();

var angular = require('../../../../../../../camunda-commons-ui/vendor/angular');

module.exports = [
  '$translate',
  function($translate) {
    return {
      restrict: 'A',
      scope: {
        deploymentsData: '='
      },

      template: template,

      controller: [
        '$scope',
        'search',
        function($scope, search) {
          var deploymentsSortingData = ($scope.deploymentsSortingData = $scope.deploymentsData.newChild(
            $scope
          ));

          var uniqueProps = ($scope.uniqueProps = {
            id: $translate.instant('REPOSITORY_DEPLOYMENTS_ID'),
            name: $translate.instant('REPOSITORY_DEPLOYMENTS_NAME'),
            deploymentTime: $translate.instant(
              'REPOSITORY_DEPLOYMENTS_DEPLOYMENT_TIME'
            )
          });

          // utilities /////////////////////////////////////////////////////////////////

          var updateSilently = function(params) {
            search.updateSilently(params);
          };

          var updateSorting = function(searchParam, value) {
            var search = {};
            search[searchParam] = value;
            updateSilently(search);
            deploymentsSortingData.changed('deploymentsSorting');
          };

          // observe data /////////////////////////////////////////////////////////////

          deploymentsSortingData.observe('deploymentsSorting', function(
            pagination
          ) {
            $scope.sorting = angular.copy(pagination);
          });

          // label ///////////////////////////////////////////////////////////////////

          $scope.byLabel = function(sortBy) {
            return uniqueProps[sortBy];
          };

          // sort order //////////////////////////////////////////////////////////////

          $scope.changeOrder = function() {
            var value = $scope.sorting.sortOrder === 'asc' ? 'desc' : 'asc';
            updateSorting('deploymentsSortOrder', value);
          };

          // sort by /////////////////////////////////////////////////////////////////

          $scope.changeBy = function(by) {
            updateSorting('deploymentsSortBy', by);
          };
        }
      ]
    };
  }
];
