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

var template = require('./cam-tasklist-filters.html?raw');

var noop = function() {};

module.exports = [
  function() {
    return {
      restrict: 'A',
      scope: {
        filtersData: '=',
        openModal: '&',
        userCanCreateFilter: '='
      },

      template: template,

      controller: [
        '$scope',
        'search',
        'camAPI',
        'Uri',
        'Notifications',
        '$translate',
        function($scope, search, camAPI, Uri, Notifications, $translate) {
          var filtersData = ($scope.filtersData = $scope.filtersData.newChild(
            $scope
          ));

          var page = ($scope.page = {
            total: 0,
            current: search().filterPage || 1,
            size: 50
          });

          function updateView() {
            $scope.filters = $scope.allFilters.slice(
              (page.current - 1) * page.size,
              page.current * page.size
            );
            search.updateSilently({
              filterPage: page.current === 1 ? null : page.current
            });
          }

          $scope.openModal = $scope.openModal() || noop;

          var filterResource = camAPI.resource('filter');

          // observe ////////////////////////////////////////////////////////////////////////////////

          $scope.$on('$locationChangeSuccess', function locationChange() {
            var params = search();

            if (
              params &&
              params.filterPage &&
              params.filterPage !== page.current
            ) {
              page.current = params.filterPage;
              updateView();
            }
          });

          /**
           * observe the count for the current filter
           */
          filtersData.observe('taskList', function(taskList) {
            $scope.filterCount = taskList.count;
          });

          /**
           * observe list of filters to set the background-color on a filter
           */
          $scope.state = filtersData.observe('filters', function(filters) {
            page.total = $scope.totalItems = filters.length;

            for (var i = 0, filter; (filter = filters[i]); i++) {
              filter.style = {
                'z-index': filters.length + 10 - i
              };
            }

            $scope.allFilters = filters.sort(function(a, b) {
              return (
                (a.properties.priority || 0) - (b.properties.priority || 0)
              );
            });
            updateView();
          });

          $scope.onPaginationchange = function() {
            updateView();
          };
          filtersData.observe('currentFilter', function(currentFilter) {
            $scope.currentFilter = currentFilter;
          });

          // selection ////////////////////////////////////////////////////////////////

          /**
           * select a filter
           */
          $scope.focus = function(filter) {
            $scope.filterCount = undefined;

            search.updateSilently({
              filter: filter.id
            });

            filtersData.changed('currentFilter');
          };

          /**
           * returns true if the provided filter is the focused filter
           */
          $scope.isFocused = function(filter) {
            return filter.id === $scope.currentFilter.id;
          };

          /**
           * Add initial 'All' filter
           */

          $scope.addAllFilter = function() {
            return $translate('ALL_TASKS')
              .then(function(translated) {
                var payload = {
                  name: translated,
                  resourceType: 'Task',
                  query: {},
                  properties: {
                    description: 'Unfiltered Tasks',
                    priority: 1,
                    color: '#555555',
                    refresh: false,
                    howUndefinedVariable: false
                  }
                };
                return filterResource.create(payload);
              })
              .then(function() {
                $scope.filtersData.changed('filters');
              })
              .catch(function(err) {
                return $translate('FILTER_SAVE_ERROR')
                  .then(function(translated) {
                    Notifications.addError({
                      status: translated,
                      message: err.message || ''
                    });
                  })
                  .catch(function() {});
              });
          };
        }
      ]
    };
  }
];
