/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/decision-definition.html', 'utf8');

var angular = require('../../../../../camunda-commons-ui/vendor/angular'),
  routeUtil = require('../../../../common/scripts/util/routeUtil'),
  camCommons = require('../../../../../camunda-commons-ui/lib');

var ngModule = angular.module('cam.cockpit.pages.decisionDefinition', [
  'dataDepend',
  camCommons.name
]);

var Controller = [
  '$scope',
  '$rootScope',
  '$q',
  'dataDepend',
  'page',
  'camAPI',
  'decisionDefinition',
  'Views',
  'search',
  'isModuleAvailable',
  '$translate',
  'queryMaxResults',
  function(
    $scope,
    $rootScope,
    $q,
    dataDepend,
    page,
    camAPI,
    decisionDefinition,
    Views,
    search,
    isModuleAvailable,
    $translate,
    queryMaxResults
  ) {
    $scope.control = {};

    var decisionData = ($scope.decisionData = dataDepend.create($scope));

    // utilities ///////////////////////

    var decisionDefinitionService = camAPI.resource('decision-definition');

    $scope.hasDrdPlugin = isModuleAvailable('cockpit.plugin.drd');
    $scope.decisionDefinition = decisionDefinition;

    // end utilities ///////////////////////

    // begin data definition //////////////////////

    decisionData.provide('decisionDefinition', decisionDefinition);

    decisionData.provide('tableXml', [
      'decisionDefinition',
      function(decisionDefinition) {
        var deferred = $q.defer();

        var decisionDefinitionId = decisionDefinition.id;

        decisionDefinitionService.getXml(decisionDefinitionId, function(
          err,
          data
        ) {
          if (!err) {
            deferred.resolve(data.dmnXml);
          } else {
            deferred.reject(err);
          }
        });

        return deferred.promise;
      }
    ]);

    decisionData.provide('allDefinitions', [
      'decisionDefinition',
      function(decisionDefinition) {
        var queryParams = {
          key: decisionDefinition.key,
          sortBy: 'version',
          sortOrder: 'desc'
        };

        if (decisionDefinition.tenantId) {
          queryParams.tenantIdIn = decisionDefinition.tenantId;
        } else {
          queryParams.withoutTenantId = true;
        }

        return queryMaxResults(
          queryParams,
          function(params) {
            return decisionDefinitionService.list(params);
          },
          function(params) {
            return decisionDefinitionService.count(params);
          }
        );
      }
    ]);

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

    $scope.breadcrumbData = decisionData.observe(
      ['decisionDefinition'],
      function(definition) {
        page.breadcrumbsClear();

        page.breadcrumbsAdd([
          {
            label: $translate.instant('DECISION_DEFINITION_DECISIONS'),
            href: '#/decisions'
          },
          {
            type: 'decisionDefinition',
            label: definition.name || definition.key || definition.id,
            href: '#/decision-definition/' + definition.id
          }
        ]);

        page.titleSet(
          [
            definition.name || definition.key || definition.id,
            $translate.instant('DECISION_DEFINITION_DEFINITION_VIEW')
          ].join(' | ')
        );
      }
    );

    decisionData.observe(['tableXml'], function(tableXml) {
      $scope.tableXml = tableXml;
    });

    $scope.initializeTablePlugins = function() {
      var tablePlugins = Views.getProviders({
        component: 'cockpit.decisionDefinition.table'
      });

      var initData = {
        decisionDefinition: decisionDefinition,
        decisionData: decisionData,
        tableControl: $scope.control
      };

      for (var i = 0; i < tablePlugins.length; i++) {
        if (typeof tablePlugins[i].initialize === 'function') {
          tablePlugins[i].initialize(initData);
        }
      }
    };

    $scope.decisionDefinitionVars = {
      read: ['decisionDefinition', 'decisionData']
    };
    $scope.decisionDefinitionTabs = Views.getProviders({
      component: 'cockpit.decisionDefinition.tab'
    });
    $scope.decisionDefinitionActions = Views.getProviders({
      component: 'cockpit.decisionDefinition.action'
    });

    // INITIALIZE PLUGINS

    var decisionPlugins = Views.getProviders({
      component: 'cockpit.decisionDefinition.tab'
    }).concat(
      Views.getProviders({component: 'cockpit.decisionDefinition.action'})
    );

    var initData = {
      decisionDefinition: decisionDefinition,
      decisionData: decisionData
    };

    for (var i = 0; i < decisionPlugins.length; i++) {
      if (typeof decisionPlugins[i].initialize === 'function') {
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
        var provider = Views.getProvider({
          component: 'cockpit.decisionDefinition.tab',
          id: selectedTabId
        });
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
        resourceName: decisionDefinition.resource,
        deploymentsQuery: JSON.stringify([
          {
            type: 'id',
            operator: 'eq',
            value: deploymentId
          }
        ])
      };

      return routeUtil.redirectTo(path, searches, [
        'deployment',
        'resourceName',
        'deploymentsQuery'
      ]);
    };
  }
];

var RouteConfig = [
  '$routeProvider',
  function($routeProvider) {
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
          decisionDefinition: [
            'ResourceResolver',
            'camAPI',
            '$q',
            function(ResourceResolver, camAPI, $q) {
              return ResourceResolver.getByRouteParam('id', {
                name: 'decision definition',
                resolve: function(id) {
                  var deferred = $q.defer();

                  var decisionDefinitionService = camAPI.resource(
                    'decision-definition'
                  );

                  decisionDefinitionService.get(id, function(err, data) {
                    if (!err) {
                      deferred.resolve(data);
                    } else {
                      deferred.reject(err);
                    }
                  });

                  return deferred.promise;
                }
              });
            }
          ]
        },
        reloadOnSearch: false
      });
  }
];

var ViewConfig = [
  'ViewsProvider',
  function(ViewsProvider) {
    ViewsProvider.registerDefaultView('cockpit.decisionDefinition.view', {
      id: 'history',
      priority: 20,
      label: 'History',
      keepSearchParams: []
    });
  }
];

ngModule.config(RouteConfig).config(ViewConfig);

module.exports = ngModule;
