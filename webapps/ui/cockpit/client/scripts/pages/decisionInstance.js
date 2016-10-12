'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/decision-instance.html', 'utf8');

var angular = require('camunda-commons-ui/vendor/angular'),
    routeUtil = require('../../../../common/scripts/util/routeUtil');

require('angular-data-depend');

var camCommons = require('camunda-commons-ui/lib');

var ngModule = angular.module('cam.cockpit.pages.decisionInstance', ['dataDepend', camCommons.name]);

var Controller = [
  '$scope', '$rootScope', '$q', 'dataDepend', 'page', 'camAPI', 'decisionInstance', 'Views', 'search',
  function($scope,   $rootScope,   $q,   dataDepend,   page,   camAPI,   decisionInstance,   Views,   search
  ) {

    $scope.control = {};

    $scope.decisionInstance = decisionInstance;

    var decisionData = $scope.decisionData = dataDepend.create($scope);

    // utilities ///////////////////////

    var decisionDefinitionService = camAPI.resource('decision-definition');

    $scope.hasCasePlugin = false;
    try {
      $scope.hasCasePlugin = !!angular.module('cockpit.plugin.case');
    }
    catch (e) {
      // do nothing
    }


    var processInstancePlugins = Views.getProviders({ component: 'cockpit.processInstance.view' });

    var hasHistoryPlugin = processInstancePlugins.filter(function(plugin) {
      return plugin.id === 'history';
    }).length > 0;

    if(hasHistoryPlugin) {
      // if we have no history plugin, then just go to the runtime view
      $scope.processInstanceLink =
          '#/process-instance/' + $scope.decisionInstance.processInstanceId + '/history' +
          '?activityInstanceIds=' + $scope.decisionInstance.activityInstanceId +
          '&activityIds=' + $scope.decisionInstance.activityId;
    } else {
      // if we have the history plugin, go to the history view and select the activity, that executed the decision
      $scope.processInstanceLink = '#/process-instance/' + $scope.decisionInstance.processInstanceId;
    }

    // end utilities ///////////////////////


    // begin data definition //////////////////////

    decisionData.provide('tableXml', function() {
      var deferred = $q.defer();

      decisionDefinitionService.getXml(decisionInstance.decisionDefinitionId, function(err, data) {
        if(!err) {
          deferred.resolve(data.dmnXml);
        } else {
          deferred.reject(err);
        }
      });

      return deferred.promise;
    });

    decisionData.provide('decisionDefinition', function() {
      var deferred = $q.defer();

      decisionDefinitionService.get(decisionInstance.decisionDefinitionId, function(err, data) {
        if(!err) {
          deferred.resolve(data);
        } else {
          deferred.reject(err);
        }
      });

      return deferred.promise;
    });

    // end data definition /////////////////////////


    // begin data usage ////////////////////////////

    // BREADCRUMBS
    $rootScope.showBreadcrumbs = true;

    page.breadcrumbsClear();

    page.breadcrumbsAdd([
      {
        label: 'Decisions',
        href: '#/decisions/'
      },
      {
        type: 'decisionDefinition',
        label: decisionInstance.decisionDefinitionName || ((decisionInstance.decisionDefinitionKey || decisionInstance.decisionDefinitionId)),
        href: '#/decision-definition/'+ decisionInstance.decisionDefinitionId
      },
      {
        type: 'decisionInstance',
        label: decisionInstance.id,
        href: '#/decision-instance/'+ decisionInstance.id
      }
    ]);

    page.titleSet([
      decisionInstance.id,
      'Instance View'
    ].join(' | '));

    decisionData.observe(['tableXml'], function(tableXml) {
      $scope.tableXml = tableXml;
    });
    decisionData.observe(['decisionDefinition'], function(decisionDefinition) {
      $scope.decisionDefinition = decisionDefinition;
    });

    $scope.getDeploymentUrl = function() {
      var path = '#/repository';

      var deploymentId = $scope.decisionDefinition.deploymentId;
      var searches = {
        deployment: deploymentId,
        resourceName: $scope.decisionDefinition.resource,
        deploymentsQuery: JSON.stringify([{
          type     : 'id',
          operator : 'eq',
          value    : deploymentId
        }])
      };

      return routeUtil.redirectTo(path, searches, [ 'deployment', 'resourceName', 'deploymentsQuery' ]);
    };

    $scope.getActivitySearch = function(decisionInstance) {

      return JSON.stringify([{
        type: 'caseActivityIdIn',
        operator: 'eq',
        value: decisionInstance.activityId
      }]);

    };

    $scope.initializeTablePlugins = function() {
      var tablePlugins = Views.getProviders({ component: 'cockpit.decisionInstance.table' });

      var initData = {
        decisionInstance   : decisionInstance,
        tableControl       : $scope.control
      };

      for(var i = 0; i < tablePlugins.length; i++) {
        if(typeof tablePlugins[i].initialize === 'function') {
          tablePlugins[i].initialize(initData);
        }
      }
    };

    $scope.decisionInstanceVars = { read: [ 'decisionInstance', 'decisionData' ] };
    $scope.decisionInstanceTabs = Views.getProviders({ component: 'cockpit.decisionInstance.tab' });
    $scope.decisionInstanceActions = Views.getProviders({ component: 'cockpit.decisionInstance.action' });


    // INITIALIZE PLUGINS

    var decisionPlugins = (
      Views.getProviders({ component: 'cockpit.decisionInstance.tab' })).concat(
      Views.getProviders({ component: 'cockpit.decisionInstance.action' })
    );

    var initData = {
      decisionInstance   : decisionInstance,
      decisionData       : decisionData
    };

    for(var i = 0; i < decisionPlugins.length; i++) {
      if(typeof decisionPlugins[i].initialize === 'function') {
        decisionPlugins[i].initialize(initData);
      }
    }

    $scope.selectTab = function(tabProvider) {
      $scope.selectedTab = tabProvider;

      search.updateSilently({
        detailsTab: tabProvider.id
      });
    };

    function setDefaultTab(tabs) {
      var selectedTabId = search().detailsTab;

      if (!tabs || !tabs.length) {
        return;
      }

      if (selectedTabId) {
        var provider = Views.getProvider({ component: 'cockpit.decisionInstance.tab', id: selectedTabId });
        if (provider && tabs.indexOf(provider) != -1) {
          $scope.selectedTab = provider;
          return;
        }
      }

      search.updateSilently({
        detailsTab: null
      });

      $scope.selectedTab = tabs[0];
    }

    setDefaultTab($scope.decisionInstanceTabs);

  }];

var RouteConfig = [
  '$routeProvider',
  function(
    $routeProvider
  ) {

    $routeProvider.when('/decision-instance/:id', {
      redirectTo: function(params, currentPath, currentSearch) {
        var redirectUrl = currentPath + '/history';

        return routeUtil.redirectTo(redirectUrl, currentSearch, true);
      }
    });

    $routeProvider
    .when('/decision-instance/:id/history', {
      template: template,

      controller: Controller,
      authentication: 'required',
      resolve: {
        decisionInstance: [ 'ResourceResolver', 'camAPI', '$q',
        function(ResourceResolver, camAPI, $q) {
          return ResourceResolver.getByRouteParam('id', {
            name: 'decision instance',
            resolve: function(id) {
              var deferred = $q.defer();

              var historyService = camAPI.resource('history');

              historyService.decisionInstance({
                decisionInstanceId: id,
                includeInputs: true,
                includeOutputs: true,
                disableBinaryFetching: true,
                disableCustomObjectDeserialization: true
              }, function(err, data) {
                if(!err && data.length) {
                  deferred.resolve(data[0]);
                } else {
                  deferred.reject(err || {
                    status: 404
                  });
                }
              });

              return deferred.promise;
            }
          });
        }]
      },
      reloadOnSearch: false
    });
  }];

var ViewConfig = [ 'ViewsProvider', function(ViewsProvider) {
  ViewsProvider.registerDefaultView('cockpit.decisionInstance.view', {
    id: 'history',
    priority: 20,
    label: 'History',
    keepSearchParams: []
  });
}];

ngModule
    .config(RouteConfig)
    .config(ViewConfig)
  ;

module.exports = ngModule;
