/* global define: false, angular: false */
define([
  'angular',
  'text!./decision-instance-table.html'
],
function(angular, template) {
  'use strict';

  return [ 'ViewsProvider', function(ViewsProvider) {

    ViewsProvider.registerDefaultView('cockpit.decisionDefinition.tab', {
      id: 'decision-instances-table',
      label: 'Decision Instances',
      template: template,
      controller: [
               '$scope', '$location', 'search', 'routeUtil', 'camAPI', 'Views', '$rootScope',
      function ($scope,   $location,   search,   routeUtil,   camAPI,   Views,   $rootScope) {

        var processInstancePlugins = Views.getProviders({ component: 'cockpit.processInstance.view' });

        var hasHistoryPlugin = processInstancePlugins.filter(function(plugin) {
          return plugin.id === 'history';
        }).length > 0;

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


        $scope.$on('$routeChanged', function() {
          pages.current = search().page || 1;
        });

        var historyService = camAPI.resource('history');

        var DEFAULT_PAGES = { size: 50, total: 0, current: search().page || 1 };

        var pages = $scope.pages = angular.copy(DEFAULT_PAGES);

        $scope.$watch('pages.current', function(newValue, oldValue) {
          if (newValue == oldValue) {
            return;
          }

          search('page', !newValue || newValue == 1 ? null : newValue);
          updateView();
        });

        updateView();

        function updateView() {
          var page = pages.current,
              count = pages.size,
              firstResult = (page - 1) * count;

          $scope.decisionInstances = null;

          $scope.loadingState = 'LOADING';
          historyService.decisionInstance({
            decisionDefinitionId: $scope.decisionDefinition.id,
            firstResult: firstResult,
            maxResults: count,
            sortBy: 'evaluationTime',
            sortOrder: 'desc'
          }, function(err, data) {
            $scope.decisionInstances = data;
            $scope.loadingState = data.length ? 'LOADED' : 'EMPTY';

            var phase = $rootScope.$$phase;
            if(phase !== '$apply' && phase !== '$digest') {
              $scope.$apply();
            }

          });

          historyService.decisionInstanceCount({
            decisionDefinitionId: $scope.decisionDefinition.id
          }, function(err, data) {
            pages.total = data.count;

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
});
