'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/task-dashboard.html', 'utf8');

module.exports = ['ViewsProvider', function(ViewsProvider) {
  ViewsProvider.registerDefaultView('cockpit.tasks.dashboard', {
    id : 'task-dashboard',
    label : 'Task Dashboard',
    template : template,
    controller : [
      '$scope', '$q', 'Views', 'camAPI', 'dataDepend',
      function($scope, $q, Views, camAPI, dataDepend) {

        var tasksPluginData = dataDepend.create($scope);

        var TaskResource       = camAPI.resource('task'),
            TaskReportResource = camAPI.resource('task-report');

        $scope.taskStatistics = [
          {
            // assigned to users
            'state' : undefined,
            'label' : 'Assigned to user',
            'count' : 0
          },
          {
            // assigned to groups
            'state' : undefined,
            'label' : 'Assigned to group',
            'count' : 0
          },
          {
            // assigned neither to groups nor to users
            'state' : undefined,
            'label' : 'Not assigned to group or user',
            'count' : 0
          }
        ];

        // -- provide task data --------------
        var provideResourceData = function(resource, method, params) {
          var deferred = $q.defer();

          var resourceCallback = function(err, res) {
            if (err) {
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

        tasksPluginData.provide('openTaskCount', function() {
          return provideResourceData(TaskResource, 'count', {});
        });

        tasksPluginData.provide('assignedToUserCount', function() {
          return provideResourceData(TaskResource, 'count', {'assigned' : true});
        });

        tasksPluginData.provide('assignedToGroupCount', function() {
          return provideResourceData(TaskResource, 'count', {'unassigned' : true, 'withCandidateGroups' : true});
        });

        tasksPluginData.provide('notAssignedCount', function() {
          return provideResourceData(TaskResource, 'count', {'unassigned' : true, 'withoutCandidateGroups' : true});
        });

        tasksPluginData.provide('countByCandidateGroup', function() {
          return provideResourceData(TaskReportResource, 'countByCandidateGroup');
        });

        // -- observe task data --------------

        $scope.openTasksState = tasksPluginData.observe(['openTaskCount'], function(_count) {
          $scope.openTasksCount = _count || 0;
        });

        $scope.taskStatistics[0].state = tasksPluginData.observe(['assignedToUserCount'], function(_userCount) {
          $scope.taskStatistics[0].count = (_userCount) || 0;
        });

        $scope.taskStatistics[1].state = tasksPluginData.observe(['assignedToGroupCount'], function(_groupCount) {
          $scope.taskStatistics[1].count = (_groupCount) || 0;
        });

        $scope.taskStatistics[2].state = tasksPluginData.observe(['notAssignedCount'], function(_notAssignedCount) {
          $scope.taskStatistics[2].count = (_notAssignedCount) || 0;
        });

        $scope.taskGroupState = tasksPluginData.observe(['countByCandidateGroup'], function(_candidateGroupCounts) {
          $scope.taskGroups = _candidateGroupCounts;
        });

        $scope.formatGroupName = function(name) {
          return ( name == null ) ? 'without group' : name;
        };
      }],

    priority : 0
  });
}];
