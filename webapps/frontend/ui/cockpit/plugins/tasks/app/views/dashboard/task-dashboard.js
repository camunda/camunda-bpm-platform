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

var template = require('./task-dashboard.html?raw');

module.exports = [
  'ViewsProvider',
  function(ViewsProvider) {
    ViewsProvider.registerDefaultView('cockpit.tasks.dashboard', {
      id: 'task-dashboard',
      label: 'PLUGIN_TASK_DASHBOARD_TITLE',
      template: template,
      controller: [
        '$scope',
        '$q',
        'Views',
        'camAPI',
        'dataDepend',
        'search',
        'Notifications',
        '$translate',
        function(
          $scope,
          $q,
          Views,
          camAPI,
          dataDepend,
          search,
          Notifications,
          $translate
        ) {
          var tasksPluginData = dataDepend.create($scope);

          var HistoryResource = camAPI.resource('history'),
            TaskReportResource = camAPI.resource('task-report');

          $scope.taskStatistics = [
            {
              // assigned to users
              state: undefined,
              label: $translate.instant('PLUGIN_TASK_ASSIGNED_TO_USER'),
              count: 0,
              search: 'openAssignedTasks'
            },
            {
              // assigned to groups
              state: undefined,
              label: $translate.instant('PLUGIN_TASK_ASSIGNED_TO_ONE'),
              count: 0,
              search: 'openGroupTasks'
            },
            {
              // assigned neither to groups nor to users
              state: undefined,
              label: $translate.instant('PLUGIN_TASK_ASSIGNED_UNASSIGNED'),
              count: 0,
              search: 'openUnassignedTasks'
            }
          ];

          // -- provide task data --------------
          var provideResourceData = function(
            resourceName,
            resource,
            method,
            params
          ) {
            var deferred = $q.defer();

            var getErrorStatus = function() {
              if (method === 'countByCandidateGroup') {
                return $translate.instant('PLUGIN_TASK_ERROR_COUNT');
              }

              return $translate.instant('PLUGIN_TASK_ERROR_FETCH', {
                res: resourceName
              });
            };

            var resourceCallback = function(err, res) {
              if (err) {
                Notifications.addError({
                  status: $translate.instant(
                    'PLUGIN_TASK_ASSIGNED_ERR_COULD_NOT',
                    {err: getErrorStatus()}
                  ),
                  message: err.toString()
                });
                deferred.reject(err);
              } else {
                deferred.resolve(res);
              }
            };

            if (params == undefined || params == null) {
              resource[method](resourceCallback);
            } else {
              resource[method](params, resourceCallback);
            }

            return deferred.promise;
          };

          var defaultParameter = function() {
            return {
              unfinished: true
            };
          };

          tasksPluginData.provide('openTaskCount', function() {
            return provideResourceData(
              $translate.instant('PLUGIN_TASK_ERROR_OPEN_TASKS'),
              HistoryResource,
              'taskCount',
              defaultParameter()
            );
          });

          tasksPluginData.provide('assignedToUserCount', function() {
            var params = defaultParameter();
            params.assigned = true;
            return provideResourceData(
              $translate.instant('PLUGIN_TASK_ERROR_ASSIGNED_TO_USERS'),
              HistoryResource,
              'taskCount',
              params
            );
          });

          tasksPluginData.provide('assignedToGroupCount', function() {
            var params = defaultParameter();
            params.unassigned = true;
            params.withCandidateGroups = true;
            return provideResourceData(
              $translate.instant('PLUGIN_TASK_ERROR_ASSIGNED_TO_GROUPS'),
              HistoryResource,
              'taskCount',
              params
            );
          });

          tasksPluginData.provide('notAssignedCount', function() {
            var params = defaultParameter();
            params.unassigned = true;
            params.withoutCandidateGroups = true;
            return provideResourceData(
              $translate.instant('PLUGIN_TASK_ERROR_UNASSIGNED'),
              HistoryResource,
              'taskCount',
              params
            );
          });

          tasksPluginData.provide('countByCandidateGroup', function() {
            return provideResourceData(
              $translate.instant('PLUGIN_TASK_ERROR_PER_GROUP'),
              TaskReportResource,
              'countByCandidateGroup'
            ).then(function(candidateGroups) {
              var groupIds = candidateGroups.map(function(group) {
                return group.groupName;
              });

              // Filter falsy values
              groupIds = groupIds.filter(Boolean);

              return camAPI
                .resource('group')
                .list({
                  idIn: groupIds,
                  maxResults: groupIds.length
                })
                .then(function(groupDefinitions) {
                  return candidateGroups.map(function(group) {
                    group.id = group.groupName;

                    var groupDef = groupDefinitions.filter(function(def) {
                      return def.id === group.groupName;
                    })[0];

                    if (groupDef) {
                      group.groupName = groupDef.name;
                    }

                    return group;
                  });
                })
                .catch(function() {
                  // When the query fails, still display the IDs
                  return candidateGroups.map(function(group) {
                    group.id = group.groupName;
                    return group;
                  });
                });
            });
          });

          // -- observe task data --------------

          $scope.openTasksState = tasksPluginData.observe(
            ['openTaskCount'],
            function(_count) {
              $scope.openTasksCount = _count.count || 0;
            }
          );

          $scope.taskStatistics[0].state = tasksPluginData.observe(
            ['assignedToUserCount'],
            function(_userCount) {
              $scope.taskStatistics[0].count = _userCount.count || 0;
            }
          );

          $scope.taskStatistics[1].state = tasksPluginData.observe(
            ['assignedToGroupCount'],
            function(_groupCount) {
              $scope.taskStatistics[1].count = _groupCount.count || 0;
            }
          );

          $scope.taskStatistics[2].state = tasksPluginData.observe(
            ['notAssignedCount'],
            function(_notAssignedCount) {
              $scope.taskStatistics[2].count = _notAssignedCount.count || 0;
            }
          );

          $scope.taskGroupState = tasksPluginData.observe(
            ['countByCandidateGroup'],
            function(_candidateGroupCounts) {
              $scope.taskGroups = _candidateGroupCounts;
            }
          );

          $scope.formatGroupName = function(name) {
            return name == null
              ? $translate.instant('PLUGIN_TASK_ASSIGNED_WITHOUT_GROUP')
              : name;
          };

          var taskDashboardPlugins = Views.getProviders({
            component: 'cockpit.tasks.dashboard'
          });
          var hasSearchPlugin = ($scope.hasSearchPlugin =
            taskDashboardPlugins.filter(function(plugin) {
              return plugin.id === 'search-tasks';
            }).length > 0);

          // -- SEARCH PLUGIN REQUIRED ------
          if (hasSearchPlugin) {
            var addTermToSearch = function(type, operator, value) {
              if (arguments.length < 3) {
                operator = 'eq';
                value = '';
              }

              return {
                type: type,
                operator: operator,
                value: value,
                name: ''
              };
            };

            // we take all data from unfinished tasks for the open task dashboard
            var resetSearch = function() {
              return [addTermToSearch('unfinished')];
            };

            var searchLinks = resetSearch();

            $scope.createSearch = function(identifier, group) {
              if (group === 'statistics') {
                switch (identifier) {
                  case 'openAssignedTasks':
                    searchLinks.push(addTermToSearch('assigned'));
                    break;
                  case 'openGroupTasks':
                    searchLinks.push(addTermToSearch('withCandidateGroups'));
                    searchLinks.push(addTermToSearch('unassigned'));
                    break;
                  case 'openUnassignedTasks':
                    searchLinks.push(addTermToSearch('withoutCandidateGroups'));
                    searchLinks.push(addTermToSearch('unassigned'));
                    break;
                }
              } else {
                if (identifier != null) {
                  searchLinks.push(
                    addTermToSearch('taskHadCandidateGroup', 'eq', identifier)
                  );
                } else {
                  // without group!
                  searchLinks.push(addTermToSearch('withoutCandidateGroups'));
                  searchLinks.push(addTermToSearch('unassigned'));
                }
              }

              search.updateSilently(
                {searchQuery: JSON.stringify(searchLinks)},
                true
              );
              searchLinks = resetSearch();
            };
          }
        }
      ],

      priority: 0
    });
  }
];
