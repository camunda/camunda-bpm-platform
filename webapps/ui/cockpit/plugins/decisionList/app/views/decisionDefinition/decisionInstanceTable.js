'use strict';

var fs = require('fs');
var angular = require('angular');
var createSearchQueryForSearchWidget = require('./../../../../../../common/scripts/util/search-widget-utils').createSearchQueryForSearchWidget;

var template = fs.readFileSync(__dirname + '/decision-instance-table.html', 'utf8');
var decisionSearchConfig = JSON.parse(fs.readFileSync(__dirname + '/decision-instance-search-config.json', 'utf8'));

module.exports = [ 'ViewsProvider', function(ViewsProvider) {

  ViewsProvider.registerDefaultView('cockpit.decisionDefinition.tab', {
    id: 'decision-instances-table',
    label: 'Decision Instances',
    template: template,
    controller: [
      '$scope', '$location', 'search', 'routeUtil', 'camAPI', 'Views', '$rootScope',
      function($scope,   $location,   search,   routeUtil,   camAPI,   Views,   $rootScope) {

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

        $scope.decisionSearchConfig = angular.copy(decisionSearchConfig);

        $scope.$watch('decisionSearchConfig.searches', function(newValue, oldValue) {
          if (!angular.equals(newValue, oldValue)) {
            updateView();
          }
        }, true);

        var historyService = camAPI.resource('history');

        $scope.onPaginationChange = function(pages) {
          $scope.pages = pages;

          if ($scope.decisionSearchConfig.searches) {
            updateView();
          }
        };

        function updateView() {
          var page = $scope.pages.current,
              count = $scope.pages.size,
              firstResult = (page - 1) * count,
              searchQuery = createSearchQueryForSearchWidget($scope.decisionSearchConfig.searches,
                ['activityIdIn', 'activityInstanceIdIn']);

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

          historyService.decisionInstance(decisionInstanceQuery, function(err, data) {
            $scope.decisionInstances = data;
            $scope.loadingState = data.length ? 'LOADED' : 'EMPTY';

            var phase = $rootScope.$$phase;
            if(phase !== '$apply' && phase !== '$digest') {
              $scope.$apply();
            }
          });

          var countQuery = angular.extend(
            {
              decisionDefinitionId: $scope.decisionDefinition.id
            },
            searchQuery
          );

          historyService.decisionInstanceCount(countQuery, function(err, data) {
            $scope.pages.total = data.count;

            var phase = $rootScope.$$phase;
            if(phase !== '$apply' && phase !== '$digest') {
              $scope.$apply();
            }

          });
        }
      }],
    priority: 10
  });
}];
