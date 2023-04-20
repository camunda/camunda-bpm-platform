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

function noop() {
  return;
}

var Directive = function() {
  return {
    replace: false,
    restrict: 'A',
    scope: {
      onSortInitialized: '&',
      onSortChange: '&',
      sortBy: '@defaultSortBy',
      sortOrder: '@defaultSortOrder',
      sortingId: '@'
    },
    controller: [
      '$scope',
      'localConf',
      function($scope, localConf) {
        var sortingId = $scope.sortingId;
        var defaultSorting = {
          sortBy: $scope.sortBy,
          sortOrder: $scope.sortOrder
        };

        var onSortInitialized = $scope.onSortInitialized || noop;
        var onSortChange = $scope.onSortChange || noop;

        var sorting = loadLocal();
        onSortInitialized({sorting: sorting});

        function loadLocal() {
          return localConf.get(sortingId, defaultSorting);
        }

        function saveLocal(sorting) {
          localConf.set(sortingId, sorting);
        }

        this.changeOrder = function(column) {
          sorting.sortBy = column;
          sorting.sortOrder = sorting.sortOrder === 'desc' ? 'asc' : 'desc';
          saveLocal(sorting);
          onSortChange({sorting: sorting});
        };

        this.getSorting = function() {
          return sorting;
        };
      }
    ]
  };
};

module.exports = Directive;
