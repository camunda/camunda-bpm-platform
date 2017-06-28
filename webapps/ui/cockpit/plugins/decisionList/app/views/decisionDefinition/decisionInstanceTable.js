'use strict';

var fs = require('fs');
var angular = require('angular');

var template = fs.readFileSync(__dirname + '/decision-instance-table.html', 'utf8');
var decisionSearchConfig = JSON.parse(fs.readFileSync(__dirname + '/decision-instance-search-config.json', 'utf8'));

module.exports = [ 'ViewsProvider', function(ViewsProvider) {

  ViewsProvider.registerDefaultView('cockpit.decisionDefinition.tab', {
    id: 'decision-instances-table',
    label: 'Decision Instances',
    template: template,
    controller: [
      '$scope', '$location', 'search', 'routeUtil', 'camAPI', 'Views',
      function($scope,   $location,   search,   routeUtil,   camAPI,   Views) {
        var processInstancePlugins = Views.getProviders({ component: 'cockpit.processInstance.view' });
        var hasHistoryPlugin = processInstancePlugins.filter(function(plugin) {
          return plugin.id === 'history';
        }).length > 0;

        $scope.hasCasePlugin = false;
        try {
          $scope.hasCasePlugin = !!angular.module('cockpit.plugin.case');
        }
        catch (e) {
          // do nothing
        }

        $scope.getProcessDefinitionLink = function(decisionInstance) {
          if(hasHistoryPlugin) {
            return '#/process-definition/' + decisionInstance.processDefinitionId + '/history';
          } else {
            return '#/process-definition/' + decisionInstance.processDefinitionId;
          }
        };

        $scope.getProcessInstanceLink = function(decisionInstance) {
          if(hasHistoryPlugin) {
            return '#/process-instance/' + decisionInstance.processInstanceId + '/history' +
            '?activityInstanceIds=' + decisionInstance.activityInstanceId +
            '&activityIds=' + decisionInstance.activityId;
          } else {
            return '#/process-instance/' + decisionInstance.processInstanceId;
          }
        };

        $scope.getActivitySearch = function(decisionInstance) {
          return JSON.stringify([{
            type: 'caseActivityIdIn',
            operator: 'eq',
            value: decisionInstance.activityId
          }]);
        };

        $scope.searchConfig = angular.copy(decisionSearchConfig);

        var historyService = camAPI.resource('history');

        $scope.onSearchChange = updateView;

        function updateView(searchQuery, pages) {
          var page = pages.current,
              count = pages.size,
              firstResult = (page - 1) * count;

          $scope.decisionInstances = null;
          $scope.loadingState = 'LOADING';

          var decisionInstanceQuery = angular.extend(
            {
              decisionDefinitionId: $scope.decisionDefinition.id,
              firstResult: firstResult,
              maxResults: count,
              sortBy: 'evaluationTime',
              sortOrder: 'desc'
            },
            searchQuery
          );

          var countQuery = angular.extend(
            {
              decisionDefinitionId: $scope.decisionDefinition.id
            },
            searchQuery
          );

          return historyService
            .decisionInstanceCount(countQuery)
            .then(function(data) {
              var total = data.count;

              return historyService
                .decisionInstance(decisionInstanceQuery)
                .then(function(data) {
                  $scope.decisionInstances = data;
                  $scope.loadingState = data.length ? 'LOADED' : 'EMPTY';

                  return total;
                });
            });
        }
      }],
    priority: 10
  });
}];
