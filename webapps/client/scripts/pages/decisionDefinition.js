/* global define: false, require: false */
define([
  'angular',
  'cockpit/util/routeUtil',
  'angular-data-depend',
  'camunda-commons-ui',
  'camunda-bpm-sdk-js',
  'text!./decision-definition.html'],
  function(
  angular,
  routeUtil,
  dataDepend,
  camCommons,
  CamSDK,
  template) {

  'use strict';

  var module = angular.module('cam.cockpit.pages.decisionDefinition', [dataDepend.name, camCommons.name]);

  var Controller = [
          '$scope', '$rootScope', '$route', '$q', 'Uri', 'search', 'ProcessDefinitionResource', 'ProcessInstanceResource', 'Views', 'Data', 'Transform', 'Variables', 'dataDepend', 'page',
  function($scope,   $rootScope,   $route,   $q,   Uri,   search,   ProcessDefinitionResource,   ProcessInstanceResource,   Views,   Data,   Transform,   Variables,   dataDepend,   page
  ) {

    var decisionData = $scope.decisionData = dataDepend.create($scope);

    // utilities ///////////////////////

    var sdk_client = new CamSDK.Client({
      apiUri: Uri.appUri('engine://'),
      engine: Uri.appUri(':engine')
    });

    var decisionDefinitionService = sdk_client.resource('decision-definition');

/*
    $scope.$on('$routeChanged', function() {
      processData.set('filter', parseFilterFromUri());
      // update tab selection
      setDefaultTab($scope.processDefinitionTabs);
    });
*/

    // end utilities ///////////////////////


    // begin data definition //////////////////////

    decisionData.provide('decisionDefinitionId', $route.current.params.id);

    decisionData.provide('decisionDefinition', [ 'decisionDefinitionId', function(decisionDefinitionId) {
      if (!decisionDefinitionId) {
        return null;
      } else {
        var deferred = $q.defer();

        decisionDefinitionService.get(decisionDefinitionId, function(err, data) {
          if(!err) {
            deferred.resolve(data);
          } else {
            deferred.reject(err);
          }
        });

        return deferred.promise;
      }
    }]);

    decisionData.provide('tableXml', ['decisionDefinitionId', function(decisionDefinitionId) {
      if (!decisionDefinitionId) {
        return null;
      } else {
        var deferred = $q.defer();

        decisionDefinitionService.getXml(decisionDefinitionId, function(err, data) {
          if(!err) {
            deferred.resolve(data.dmnXml);
          } else {
            deferred.reject(err);
          }
        });

        return deferred.promise;
      }
    }]);

    decisionData.provide('allDefinitions', [ 'decisionDefinition', function(decisionDefinition) {
      if (!decisionDefinition) {
        return null;
      } else {
        var deferred = $q.defer();

        decisionDefinitionService.list({ key: decisionDefinition.key }, function(err, data) {
          if(!err) {
            deferred.resolve(data);
          } else {
            deferred.reject(err);
          }
        });

        return deferred.promise;
      }
    }]);

    // end data definition /////////////////////////


    // begin data usage ////////////////////////////

    decisionData.observe(['decisionDefinition'], function(decisionDefinition) {
      $scope.decisionDefinition = decisionDefinition;
    });
    decisionData.observe(['allDefinitions'], function(allDefinitions) {
      $scope.allDefinitions = allDefinitions;
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
        'camunda Cockpit',
        definition.name || definition.key || definition.id,
        'Definition View'
      ].join(' | '));
    });

    decisionData.observe(['tableXml'], function(tableXml) {
      $scope.tableXml = tableXml;
    });

/*
    $scope.instanceStatistics = processData.observe([ 'instances.all', 'instances.current' ], function(allCount, currentCount) {
      $scope.instanceStatistics.all = allCount;
      $scope.instanceStatistics.current = currentCount;
    });
*/


    // INITIALIZE PLUGINS

/*
    var processPlugins = (
        Views.getProviders({ component: 'cockpit.processDefinition.runtime.tab' })).concat(
        Views.getProviders({ component: 'cockpit.processDefinition.runtime.action' })).concat(
        Views.getProviders({ component: 'cockpit.processDefinition.view' })).concat(
        Views.getProviders({ component: 'cockpit.processDefinition.diagram.overlay' })).concat(
        Views.getProviders({ component: 'cockpit.jobDefinition.action' }));
*/
  }];

  var RouteConfig = [
    '$routeProvider',
  function(
    $routeProvider
  ) {

    $routeProvider
    .when('/decision-definition/:id', {
      template: template,

      controller: Controller,
      authentication: 'required',
      resolve: {
        /*processDefinition: [ 'ResourceResolver', 'ProcessDefinitionResource',
          function(ResourceResolver, ProcessDefinitionResource) {
            return ResourceResolver.getByRouteParam('id', {
              name: 'process definition',
              resolve: function(id) {
                return ProcessDefinitionResource.get({ id : id });
              }
            });
          }]*/
      },
      reloadOnSearch: false
    });
  }];

/*
  var ViewConfig = [ 'ViewsProvider', function(ViewsProvider) {
    ViewsProvider.registerDefaultView('cockpit.processDefinition.view', {
      id: 'runtime',
      priority: 20,
      label: 'Runtime',
      keepSearchParams: [
        'parentProcessDefinitionId',
        'businessKey',
        'variables',
        'startedAfter',
        'startedBefore',
        'viewbox'
      ]
    });
  }];
*/

  module
    .config(RouteConfig)
  ;

  return module;
  });
