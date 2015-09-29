/* global define: false, require: false */
define([
  'angular',
  'cockpit/util/routeUtil',
  'angular-data-depend',
  'camunda-commons-ui',
  'text!./decision-definition.html'],
  function(
  angular,
  routeUtil,
  dataDepend,
  camCommons,
  template) {

  'use strict';

  var module = angular.module('cam.cockpit.pages.decisionDefinition', [dataDepend.name, camCommons.name]);

  var Controller = [
          '$scope', '$rootScope', '$q', 'dataDepend', 'page', 'camAPI', 'decisionDefinition', 'Views', 'search',
  function($scope,   $rootScope,   $q,   dataDepend,   page,   camAPI,   decisionDefinition,   Views,   search
  ) {

    var decisionData = $scope.decisionData = dataDepend.create($scope);

    // utilities ///////////////////////

    var decisionDefinitionService = camAPI.resource('decision-definition');
    var Deployment = camAPI.resource('deployment');

    var resource;

    // end utilities ///////////////////////


    // begin data definition //////////////////////

    decisionData.provide('decisionDefinition', decisionDefinition);

    decisionData.provide('tableXml', ['decisionDefinition', function(decisionDefinition) {
      var deferred = $q.defer();

      var decisionDefinitionId = decisionDefinition.id;

      decisionDefinitionService.getXml(decisionDefinitionId, function(err, data) {
        if(!err) {
          deferred.resolve(data.dmnXml);
        } else {
          deferred.reject(err);
        }
      });

      return deferred.promise;
    }]);

    decisionData.provide('allDefinitions', [ 'decisionDefinition', function(decisionDefinition) {
      var deferred = $q.defer();

      decisionDefinitionService.list({ key: decisionDefinition.key }, function(err, data) {
        if(!err) {
          deferred.resolve(data);
        } else {
          deferred.reject(err);
        }
      });

      return deferred.promise;
    }]);

    decisionData.provide('resources', [ 'decisionDefinition', function(decisionDefinition) {
      var deferred = $q.defer();

      Deployment.getResources(decisionDefinition.deploymentId, function(err, res) {
        if(err) {
          deferred.reject(err);
        }
        else {
          deferred.resolve(res);
        }
      });

      return deferred.promise;
    }]);

    decisionData.provide('resource', [ 'decisionDefinition', 'resources', function(decisionDefinition, resources) {
      var resource;
      for (var i = 0, _resource; !!(_resource = resources[i]); i++) {
        if (_resource.name === decisionDefinition.resource) {
          resource = _resource;
          break;
        }
      }
      return resource;
    }]);

    // end data definition /////////////////////////


    // begin data usage ////////////////////////////

    decisionData.observe(['decisionDefinition'], function(decisionDefinition) {
      $scope.decisionDefinition = decisionDefinition;
    });
    decisionData.observe(['allDefinitions'], function(allDefinitions) {
      $scope.allDefinitions = allDefinitions;
    });
    decisionData.observe('resource', function(_resource) {
      resource = _resource;
    });

    // BREADCRUMBS
    $rootScope.showBreadcrumbs = true;

    $scope.breadcrumbData = decisionData.observe([ 'decisionDefinition' ], function(definition) {
      page.breadcrumbsClear();

      page.breadcrumbsAdd({
        type: 'decisionDefinition',
        label: definition.name || definition.key || definition.id,
        href: '#/decision-definition/'+ definition.id
      });

      page.titleSet([
        'Camunda Cockpit',
        definition.name || definition.key || definition.id,
        'Definition View'
      ].join(' | '));
    });

    decisionData.observe(['tableXml'], function(tableXml) {
      $scope.tableXml = tableXml;
    });

    $scope.decisionDefinitionVars = { read: [ 'decisionDefinition', 'decisionData' ] };
    $scope.decisionDefinitionTabs = Views.getProviders({ component: 'cockpit.decisionDefinition.tab' });
    $scope.decisionDefinitionActions = Views.getProviders({ component: 'cockpit.decisionDefinition.action' });


    // INITIALIZE PLUGINS

    var decisionPlugins = (
      Views.getProviders({ component: 'cockpit.decisionDefinition.tab' })).concat(
      Views.getProviders({ component: 'cockpit.decisionDefinition.action' })
    );

    var initData = {
      decisionDefinition : $scope.decisionDefinitionService,
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
        var provider = Views.getProvider({ component: 'cockpit.decisionDefinition.tab', id: selectedTabId });
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

    setDefaultTab($scope.decisionDefinitionTabs);

    $scope.getDeploymentUrl = function() {
      var path = '#/repository';

      var deploymentId = decisionDefinition.deploymentId;
      var searches = {
        deployment: deploymentId,
        resource: resource ? resource.id : null,
        deploymentsQuery: JSON.stringify([{
          type     : 'id',
          operator : 'eq',
          value    : deploymentId
        }])
      };

      return routeUtil.redirectTo(path, searches, [ 'deployment', 'resource', 'deploymentsQuery' ]);
    };

  }];

  var RouteConfig = [
    '$routeProvider',
  function(
    $routeProvider
  ) {

    $routeProvider
    .when('/decision-definition/:id', {
      redirectTo: function(params, currentPath, currentSearch) {
        var redirectUrl = currentPath + '/history';

        return routeUtil.redirectTo(redirectUrl, currentSearch, true);
      }
    })
    .when('/decision-definition/:id/history', {
      template: template,

      controller: Controller,
      authentication: 'required',
      resolve: {
        decisionDefinition: [ 'ResourceResolver', 'camAPI', '$q',
        function (ResourceResolver, camAPI, $q) {
          return ResourceResolver.getByRouteParam('id', {
            name: 'decision definition',
            resolve: function (id) {

              var deferred = $q.defer();

              var decisionDefinitionService = camAPI.resource('decision-definition');

              decisionDefinitionService.get(id, function(err, data) {
                if(!err) {
                  deferred.resolve(data);
                } else {
                  deferred.reject(err);
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
    ViewsProvider.registerDefaultView('cockpit.decisionDefinition.view', {
      id: 'history',
      priority: 20,
      label: 'History',
      keepSearchParams: []
    });
  }];

  module
    .config(RouteConfig)
    .config(ViewConfig)
  ;

  return module;
  });
