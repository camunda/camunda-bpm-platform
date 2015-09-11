/* global define: false, require: false */
define([
  'angular',
  'cockpit/util/routeUtil',
  'angular-data-depend',
  'camunda-commons-ui',
  'text!./decision-instance.html'],
  function(
  angular,
  routeUtil,
  dataDepend,
  camCommons,
  template) {

  'use strict';

  var module = angular.module('cam.cockpit.pages.decisionInstance', [dataDepend.name, camCommons.name]);

  var Controller = [
          '$scope', '$rootScope', '$q', 'dataDepend', 'page', 'camAPI', 'decisionInstance', 'Views', 'search',
  function($scope,   $rootScope,   $q,   dataDepend,   page,   camAPI,   decisionInstance,   Views,   search
  ) {

    $scope.decisionInstance = decisionInstance;

    var decisionData = $scope.decisionData = dataDepend.create($scope);

    // utilities ///////////////////////

    var decisionDefinitionService = camAPI.resource('decision-definition');

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

    // end data definition /////////////////////////


    // begin data usage ////////////////////////////

    // BREADCRUMBS
    $rootScope.showBreadcrumbs = true;

    page.breadcrumbsClear();

    page.breadcrumbsAdd({
      type: 'decisionDefinition',
      label: decisionInstance.decisionDefinitionName || ((decisionInstance.decisionDefinitionKey || decisionInstance.decisionDefinitionId).slice(0, 8) +'…'),
      href: '#/decision-definition/'+ decisionInstance.decisionDefinitionId
    });

    page.breadcrumbsAdd({
      type: 'decisionInstance',
      label: decisionInstance.id.slice(0, 8) +'…',
      href: '#/decision-instance/'+ decisionInstance.id
    });

    page.titleSet([
      'camunda Cockpit',
      decisionInstance.id.slice(0, 8) +'…',
      'Instance View'
    ].join(' | '));

    decisionData.observe(['tableXml'], function(tableXml) {
      $scope.tableXml = tableXml;
    });

    $scope.decisionInstanceVars = { read: [ 'decisionInstance', 'decisionData' ] };
    $scope.decisionInstanceTabs = Views.getProviders({ component: 'cockpit.decisionInstance.tab' });


    // INITIALIZE PLUGINS

    var decisionPlugins = Views.getProviders({ component: 'cockpit.decisionInstance.tab' });

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

    $routeProvider
    .when('/decision-instance/:id', {
      template: template,

      controller: Controller,
      authentication: 'required',
      resolve: {
        decisionInstance: [ 'ResourceResolver', 'camAPI', '$q',
        function (ResourceResolver, camAPI, $q) {
          return ResourceResolver.getByRouteParam('id', {
            name: 'decision instance',
            resolve: function (id) {
              var deferred = $q.defer();

              var historyService = camAPI.resource('history');

              historyService.decisionInstance({
                decisionInstanceId: id,
                includeInputs: true,
                includeOutputs: true,
                disableBinaryFetching: true,
                disableCustomObjectDeserialization: true
              }, function(err, data) {
                if(!err) {
                  deferred.resolve(data[0]);
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

  module
    .config(RouteConfig)
  ;

  return module;
  });
