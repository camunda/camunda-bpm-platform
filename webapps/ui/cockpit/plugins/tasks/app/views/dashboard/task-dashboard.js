'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/task-dashboard.html', 'utf8');

module.exports = ['ViewsProvider', function(ViewsProvider) {
  ViewsProvider.registerDefaultView('cockpit.tasks.dashboard', {
    id : 'task-dashboard',
    label : 'Task Dashboard',
    template : template,
    controller : [
      '$scope', '$q', 'Views', 'camAPI', 'dataDepend', 'search', 'Notifications',
      function($scope, $q, Views, camAPI, dataDepend, search, Notifications) {

        var tasksPluginData = dataDepend.create($scope);

        var HistoryResource       = camAPI.resource('history'),
            TaskReportResource = camAPI.resource('task-report');

        $scope.taskStatistics = [
          {
            // assigned to users
            'state' : undefined,
            'label' : 'assigned to a user',
            'count' : 0,
            'search': 'openAssignedTasks'
          },
          {
            // assigned to groups
            'state' : undefined,
            'label' : 'assigned to 1 or more groups',
            'count' : 0,
            'search': 'openGroupTasks'
          },
          {
            // assigned neither to groups nor to users
            'state' : undefined,
            'label' : 'unassigned',
            'count' : 0,
            'search': 'openUnassignedTasks'
          }
        ];

        // -- provide task data --------------
        var provideResourceData = function(resourceName, resource, method, params) {
          var deferred = $q.defer();

          var resourceCallback = function(err, res) {
            if (err) {
              Notifications.addError({
                status: 'Could not fetch the resource for \'' + resourceName + '\'',
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
          return provideResourceData('Open tasks', HistoryResource, 'taskCount', defaultParameter());
        });

        tasksPluginData.provide('assignedToUserCount', function() {
          var params = defaultParameter();
          params.assigned = true;
          return provideResourceData('Tasks assigned to users', HistoryResource, 'taskCount', params);
        });

        tasksPluginData.provide('assignedToGroupCount', function() {
          var params = defaultParameter();
          params.unassigned = true;
          params.withCandidateGroups = true;
          return provideResourceData('Tasks assigned to groups', HistoryResource, 'taskCount', params);
        });

        tasksPluginData.provide('notAssignedCount', function() {
          var params = defaultParameter();
          params.unassigned = true;
          params.withoutCandidateGroups = true;
          return provideResourceData('Unassigned tasks', HistoryResource, 'taskCount', params);
        });

        tasksPluginData.provide('countByCandidateGroup', function() {
          return provideResourceData('Tasks per group', TaskReportResource, 'countByCandidateGroup');
        });

        // -- observe task data --------------

        $scope.openTasksState = tasksPluginData.observe(['openTaskCount'], function(_count) {
          $scope.openTasksCount = _count.count || 0;
        });

        $scope.taskStatistics[0].state = tasksPluginData.observe(['assignedToUserCount'], function(_userCount) {
          $scope.taskStatistics[0].count = (_userCount.count) || 0;
        });

        $scope.taskStatistics[1].state = tasksPluginData.observe(['assignedToGroupCount'], function(_groupCount) {
          $scope.taskStatistics[1].count = (_groupCount.count) || 0;
        });

        $scope.taskStatistics[2].state = tasksPluginData.observe(['notAssignedCount'], function(_notAssignedCount) {
          $scope.taskStatistics[2].count = (_notAssignedCount.count) || 0;
        });

        $scope.taskGroupState = tasksPluginData.observe(['countByCandidateGroup'], function(_candidateGroupCounts) {
          $scope.taskGroups = _candidateGroupCounts;
        });

        $scope.formatGroupName = function(name) {
          return ( name == null ) ? 'without group' : name;
        };

        var taskDashboardPlugins = Views.getProviders({component: 'cockpit.tasks.dashboard'});
        var hasSearchPlugin = $scope.hasSearchPlugin = taskDashboardPlugins.filter(function(plugin) {
          return plugin.id === 'search-tasks';
        }).length > 0;

        // -- SEARCH PLUGIN REQUIRED ------
        if (hasSearchPlugin) {
          var addTermToSearch = function(type, operator, value) {
            if(arguments.length < 3) {
              operator = 'eq';
              value = '';
            }

            return {
              'type' : type,
              'operator' : operator,
              'value' : value,
              'name' : ''
            };
          };

          // we take all data from unfinished tasks for the open task dashboard
          var resetSearch = function() {
            return [ addTermToSearch('unfinished') ];
          };

          var searchLinks = resetSearch();

          $scope.createSearch = function(identifier, group) {
            if(group === 'statistics') {
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
              if(identifier != null) {
                searchLinks.push(addTermToSearch('taskHadCandidateGroup', 'eq', identifier));
              } else {
                // without group!
                searchLinks.push(addTermToSearch('withoutCandidateGroups'));
                searchLinks.push(addTermToSearch('unassigned'));
              }
            }

            search.updateSilently({ searchQuery: JSON.stringify(searchLinks) }, true);
            searchLinks = resetSearch();
          };

          // prevents the initializer from overwriting the exisiting search
          if(!search().hasOwnProperty('searchQuery')) {
            search.updateSilently({ searchQuery: JSON.stringify(searchLinks) }, true);
          }
        }
      }],

    priority : 0
  });
}];
