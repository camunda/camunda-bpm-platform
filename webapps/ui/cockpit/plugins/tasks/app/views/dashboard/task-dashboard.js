'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/task-dashboard.html', 'utf8');

module.exports = [ 'ViewsProvider', function(ViewsProvider) {
  ViewsProvider.registerDefaultView('cockpit.tasks.dashboard', {
    id : 'task-dashboard',
    label : 'Task Dashboard',
    template : template,
    controller : [
      '$scope', '$q', 'camAPI', 'dataDepend',
      function($scope, $q, camAPI, dataDepend) {

        var tasksPluginData = dataDepend.create($scope);

        var TaskResource       = camAPI.resource('task'),
            TaskReportResource = camAPI.resource('task-report');

        $scope.taskCountStatistic = [
          {
            // assigned to users
            'loadingState' : undefined,
            'label' : 'Assigned to user',
            'count' : 0
          },
          {
            // assigned to groups
            'loadingState' : undefined,
            'label' : 'Assigned to group',
            'count' : 0
          },
          {
            // assigned neither to groups nor to users
            'loadingState' : undefined,
            'label' : 'Not assigned to group or user',
            'count' : 0
          }
        ];

        // -- provide task data --------------
        var provideResourceData = function(resource, method, params) {
          var deferred = $q.defer();

          var resourceCallback = function(err, res) {
            if( err ) {
              deferred.reject(err);
            } else {
              deferred.resolve(res);
            }
          };

          if( params == undefined || params == null ) {
            resource[ method ](resourceCallback);
          } else {
            resource[ method ](params, resourceCallback);
          }

          return deferred.promise;
        };

        tasksPluginData.provide('openTaskCount', function() {
          return provideResourceData(TaskResource, 'count', {});
        });

        tasksPluginData.provide('unassignedTaskCount', function() {
          return provideResourceData(TaskResource, 'count', { 'unassigned' : true });
        });

        tasksPluginData.provide('countByCandidateGroup', function() {
          return provideResourceData(TaskReportResource, 'countByCandidateGroup');
        });

        // -- observe task data --------------

        $scope.openTaskLoadingState = tasksPluginData.observe([ 'openTaskCount' ], function(_count) {
          $scope.openTasksCount = _count || 0;
        });

        $scope.taskCountStatistic[ 0 ].loadingState = tasksPluginData.observe([ 'openTaskCount', 'unassignedTaskCount' ],
        function(_openCount, unassignedCount) {
          $scope.taskCountStatistic[ 0 ].count = (_openCount - unassignedCount) || 0;
        });

        $scope.taskCountStatistic[ 1 ].loadingState = $scope.taskCountStatistic[ 2 ].loadingState = tasksPluginData.observe(
        [ 'unassignedTaskCount', 'countByCandidateGroup' ],
        function(_unassignedCount, _candidateObj) {
          $scope.taskCountObjects = _candidateObj;

          var totalCandidateCount = 0,
              candidateCount      = 0;

          for( var i = 0, len = _candidateObj.length; i < len; i++ ) {
            var candidate = _candidateObj[ i ];
            totalCandidateCount += candidate.taskCount;

            if( candidate.groupName == null ) {
              var noCount = candidate.taskCount;
              continue;
            }

            candidateCount += candidate.taskCount
          }

          var multipleCandidateCount = (totalCandidateCount - _unassignedCount) || 0;
          $scope.taskCountStatistic[ 1 ].count = (candidateCount - multipleCandidateCount) || 0;
          $scope.taskCountStatistic[ 2 ].count = noCount || 0;
        });

        $scope.formatGroupName = function(name) {
          return ( name == null ) ? 'without group' : name;
        }
      } ],

    priority : 0
  });
} ];
