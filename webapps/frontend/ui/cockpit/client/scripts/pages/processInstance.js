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

var template = require('./process-instance.html')();

var angular = require('../../../../../camunda-commons-ui/vendor/angular');
var commonModule = require('../../../../common/scripts/module');

var routeUtil = require('../../../../common/scripts/util/routeUtil');
var camCommons = require('../../../../../camunda-commons-ui/lib');

var ngModule = angular.module('cam.cockpit.pages.processInstance', [
  camCommons.name,
  commonModule.name,
  'dataDepend'
]);

var Controller = [
  '$scope',
  '$filter',
  '$rootScope',
  '$location',
  'search',
  'ProcessDefinitionResource',
  'ProcessInstanceResource',
  'IncidentResource',
  'Views',
  'Data',
  'Transform',
  'processInstance',
  'dataDepend',
  'page',
  'breadcrumbTrails',
  'integrateActivityInstanceFilter',
  'isModuleAvailable',
  '$translate',
  'camAPI',
  function(
    $scope,
    $filter,
    $rootScope,
    $location,
    search,
    ProcessDefinitionResource,
    ProcessInstanceResource,
    IncidentResource,
    Views,
    Data,
    Transform,
    processInstance,
    dataDepend,
    page,
    breadcrumbTrails,
    integrateActivityInstanceFilter,
    isModuleAvailable,
    $translate,
    camAPI
  ) {
    $scope.hasMigrationPlugin = isModuleAvailable('cockpit.plugin.migration');
    $scope.processInstance = processInstance;

    var processData = ($scope.processData = dataDepend.create($scope));
    var pageData = ($scope.pageData = dataDepend.create($scope));

    $scope.diagramCollapsed = true;
    $scope.onDiagramCollapseChange = function(collapsed) {
      if (!$scope.$$phase) {
        $scope.$apply(function() {
          $scope.diagramCollapsed = collapsed;
        });
      } else {
        $scope.diagramCollapsed = collapsed;
      }
    };

    // utilities ///////////////////////

    $scope.hovered = null;
    $scope.hoverTitle = function(id) {
      $scope.hovered = id || null;
    };

    // end utilities ///////////////////////

    // /////// Begin definition of process data

    // processInstance
    processData.provide('processInstance', processInstance);

    // processDefinition
    processData.provide('processDefinition', [
      'processInstance',
      function(processInstance) {
        return ProcessDefinitionResource.get({id: processInstance.definitionId})
          .$promise;
      }
    ]);

    processData.provide('bpmn20Xml', [
      'processDefinition',
      function(definition) {
        return ProcessDefinitionResource.getBpmn20Xml({id: definition.id})
          .$promise;
      }
    ]);

    // bpmnElements
    processData.provide('parsedBpmn20', [
      'bpmn20Xml',
      function(bpmn20Xml) {
        return Transform.transformBpmn20Xml(bpmn20Xml.bpmn20Xml);
      }
    ]);

    processData.provide('bpmnElements', [
      'parsedBpmn20',
      function(parsedBpmn20) {
        return parsedBpmn20.bpmnElements;
      }
    ]);

    processData.provide('bpmnDefinition', [
      'parsedBpmn20',
      function(parsedBpmn20) {
        return parsedBpmn20.definitions;
      }
    ]);

    // activityInstances
    processData.provide('activityInstances', [
      'processInstance',
      function(processInstance) {
        return ProcessInstanceResource.activityInstances({
          id: processInstance.id
        }).$promise;
      }
    ]);

    // activityInstanceTree, activityIdToInstancesMap, instanceIdToInstanceMap
    processData.provide(
      [
        'activityInstanceTree',
        'activityIdToInstancesMap',
        'instanceIdToInstanceMap',
        'activityIdToIncidentIdMap'
      ],
      [
        'activityInstances',
        'processDefinition',
        'bpmnElements',
        function(activityInstances, processDefinition, bpmnElements) {
          var activityIdToInstancesMap = {},
            instanceIdToInstanceMap = {},
            model = bpmnElements[processDefinition.key],
            activityIdToIncidentIdMap = {};

          function getActivityName(bpmnElement) {
            var name = bpmnElement.name;
            if (!name) {
              var shortenFilter = $filter('shorten');
              name =
                bpmnElement.$type.substr(5) +
                ' (' +
                shortenFilter(bpmnElement.id, 8) +
                ')';
            }

            return name;
          }

          function addIncidents(incidents) {
            incidents.forEach(function(incident) {
              var incidentIds =
                activityIdToIncidentIdMap[incident.activityId] || [];
              incidentIds.push(incident.id);
              activityIdToIncidentIdMap[incident.activityId] = incidentIds;
            });
          }

          function decorateActivityInstanceTree(instance) {
            var children = instance.childActivityInstances;

            if (children && children.length > 0) {
              for (var i = 0, child; (child = children[i]); i++) {
                var activityId = child.activityId,
                  bpmnElement = bpmnElements[activityId],
                  instances = activityIdToInstancesMap[activityId] || [];

                if (bpmnElement) {
                  child.name = getActivityName(bpmnElement);
                } else {
                  child.name = activityId;
                }
                child.isTransitionInstance = false;
                activityIdToInstancesMap[activityId] = instances;
                if (!instanceIdToInstanceMap[child.id]) {
                  instanceIdToInstanceMap[child.id] = child;
                }

                addIncidents(child.incidents);

                instances.push(child);

                decorateActivityInstanceTree(child);
              }
            }

            var transitions = instance.childTransitionInstances;
            if (transitions && transitions.length > 0) {
              for (var t = 0, transition; (transition = transitions[t]); t++) {
                var targetActivityId = transition.targetActivityId,
                  transitionBpmnElement = bpmnElements[targetActivityId],
                  transitionInstances =
                    activityIdToInstancesMap[targetActivityId] || [];

                if (transitionBpmnElement) {
                  transition.name = getActivityName(transitionBpmnElement);
                } else {
                  transition.name = targetActivityId;
                }
                transition.isTransitionInstance = true;
                activityIdToInstancesMap[
                  targetActivityId
                ] = transitionInstances;
                if (!instanceIdToInstanceMap[transition.id]) {
                  instanceIdToInstanceMap[transition.id] = transition;
                }

                addIncidents(transition.incidents);

                transitionInstances.push(transition);
              }
            }
          }

          activityInstances.name = getActivityName(model);

          // add initially the root to the map
          instanceIdToInstanceMap[activityInstances.id] = activityInstances;

          decorateActivityInstanceTree(activityInstances);

          return [
            activityInstances,
            activityIdToInstancesMap,
            instanceIdToInstanceMap,
            activityIdToIncidentIdMap
          ];
        }
      ]
    );

    processData.provide('executionIdToInstanceMap', [
      'instanceIdToInstanceMap',
      function(instanceIdToInstanceMap) {
        var executionIdToInstanceMap = {};

        for (var key in instanceIdToInstanceMap) {
          var instance = instanceIdToInstanceMap[key],
            executionIds = instance.executionIds,
            executionId = instance.executionId;

          if (executionIds) {
            for (var i = 0, execId; (execId = executionIds[i]); i++) {
              executionIdToInstanceMap[execId] = instance;
            }
          }

          if (executionId) {
            executionIdToInstanceMap[executionId] = instance;
          }
        }

        return executionIdToInstanceMap;
      }
    ]);

    // processDiagram
    processData.provide('processDiagram', [
      'bpmnDefinition',
      'bpmnElements',
      function(bpmnDefinition, bpmnElements) {
        var processDiagram = {};

        processDiagram.bpmnDefinition = bpmnDefinition;
        processDiagram.bpmnElements = bpmnElements;

        return processDiagram;
      }
    ]);

    processData.provide('latestDefinition', [
      'processDefinition',
      function(definition) {
        var queryParams = {
          key: definition.key,
          sortBy: 'version',
          sortOrder: 'desc',
          latestVersion: true,
          maxResults: 1
        };

        if (definition.tenantId) {
          queryParams.tenantIdIn = [definition.tenantId];
        } else {
          queryParams.withoutTenantId = true;
        }
        return camAPI.resource('process-definition').list(queryParams);
      }
    ]);

    // /////// End definition of process data
    integrateActivityInstanceFilter($scope, angular.noop, {
      shouldRemoveActivityIds: true
    });

    // /////// Begin usage of definied process data

    $scope.processDefinition = processData.observe(
      'processDefinition',
      function(processDefinition) {
        $scope.processDefinition = processDefinition;
      }
    );

    $scope.latestProcessDefinition = processData.observe(
      'latestDefinition',
      function(processDefinition) {
        $scope.latestProcessDefinition = processDefinition.items[0];
      }
    );

    $scope.isLatestVersion = function() {
      return (
        $scope.processDefinition.version ===
        $scope.latestProcessDefinition.version
      );
    };

    $scope.getMigrationUrl = function() {
      var path = '#/migration';

      var searches = {
        sourceKey: $scope.processDefinition.key,
        targetKey: $scope.latestProcessDefinition.key,
        sourceVersion: $scope.processDefinition.version,
        targetVersion: $scope.latestProcessDefinition.version,
        searchQuery: JSON.stringify([
          {
            type: 'processInstanceIds',
            operator: 'eq',
            value: $scope.processInstance.id
          }
        ])
      };

      return routeUtil.redirectTo(path, searches, [
        'sourceKey',
        'targetKey',
        'sourceVersion',
        'targetVersion',
        'searchQuery'
      ]);
    };

    $scope.getDeploymentUrl = function() {
      var path = '#/repository';

      var deploymentId = $scope.processDefinition.deploymentId;
      var searches = {
        deployment: deploymentId,
        resourceName: $scope.processDefinition.resource,
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

    processData.provide('superProcessInstance', [
      'processInstance',
      function(processInstance) {
        return ProcessInstanceResource.query(
          {maxResults: 1},
          {subProcessInstance: processInstance.id}
        ).$promise;
      }
    ]);

    function fetchSuperProcessInstance(processInstance, done) {
      ProcessInstanceResource.query(
        {maxResults: 1},
        {
          subProcessInstance: processInstance.id
        }
      )
        .$promise.then(function(response) {
          var superInstance = response[0];

          done(null, superInstance);
        })
        .catch(angular.noop);
    }

    $rootScope.showBreadcrumbs = true;

    processData.observe(
      ['processDefinition', 'processInstance', 'superProcessInstance'],
      function(processDefinition, processInstance, superProcessInstance) {
        var crumbs = [
          {
            label: $translate.instant('PROCESS_INSTANCE_PROCESSES'),
            href: '#/processes/'
          }
        ];

        if (superProcessInstance.length) {
          $scope.superProcessInstance = superProcessInstance[0];

          crumbs.push(function(index) {
            breadcrumbTrails(
              processInstance,
              fetchSuperProcessInstance,
              [],
              index,
              'runtime'
            );
          });
        }

        crumbs.push({
          label:
            processDefinition.name ||
            processDefinition.key ||
            processDefinition.id,
          href: '#/process-definition/' + processDefinition.id + '/runtime',
          keepSearchParams: ['viewbox']
        });

        var plugins = Views.getProviders({
          component: 'cockpit.processInstance.view'
        });

        crumbs.push({
          type: 'processInstance',
          divider: ':',
          label:
            processInstance.name || processInstance.key || processInstance.id,
          href: '#/process-instance/' + processInstance.id + '/runtime',
          processInstance: processInstance,

          choices: plugins
            .sort(function(a, b) {
              return a.priority < b.priority
                ? -1
                : a.priority > b.priority
                ? 1
                : 0;
            })
            .map(function(plugin) {
              return {
                active: plugin.id === 'runtime',
                label: plugin.label,
                href:
                  '#/process-instance/' + processInstance.id + '/' + plugin.id
              };
            })
        });

        page.breadcrumbsClear().breadcrumbsAdd(crumbs);

        page.titleSet(
          [
            $scope.processDefinition.name || $scope.processDefinition.id,
            $translate.instant('PROCESS_INSTANCE_INSTANCE_VIEW')
          ].join(' | ')
        );
      }
    );

    $scope.activityInstanceTree = processData.observe(
      'activityInstanceTree',
      function(activityInstanceTree) {
        $scope.activityInstanceTree = activityInstanceTree;
      }
    );

    $scope.processDiagram = processData.observe('processDiagram', function(
      processDiagram
    ) {
      $scope.processDiagram = processDiagram;
    });

    processData.observe(
      ['instanceIdToInstanceMap', 'activityIdToInstancesMap'],
      function(instanceIdToInstanceMap, activityIdToInstancesMap) {
        $scope.instanceIdToInstanceMap = instanceIdToInstanceMap;
        $scope.activityIdToInstancesMap = activityIdToInstancesMap;
      }
    );

    // /////// End of usage of definied process data

    $scope.handleBpmnElementSelection = function(id, $event) {
      if (!id) {
        processData.set('filter', {});
        return;
      }

      var ctrlKey = $event.ctrlKey,
        activityIds = angular.copy($scope.filter.activityIds) || [],
        activityInstanceIds =
          angular.copy($scope.filter.activityInstanceIds) || [],
        idx = activityIds.indexOf(id),
        instanceList = angular.copy($scope.activityIdToInstancesMap[id]) || [],
        multiInstance =
          $scope.activityIdToInstancesMap[id + '#multiInstanceBody'],
        newFilter;

      if (multiInstance) {
        Array.prototype.push.apply(instanceList, multiInstance);
        if (idx === -1) {
          idx = activityIds.indexOf(id + '#multiInstanceBody');
        }
      }

      if (!ctrlKey) {
        activityIds = [id];
        if (multiInstance) {
          activityIds.push(id + '#multiInstanceBody');
        }

        activityInstanceIds = [];
        angular.forEach(instanceList, function(instance) {
          activityInstanceIds.push(instance.id);
        });
      } else if (ctrlKey) {
        if (idx === -1) {
          activityIds.push(id);
          if (multiInstance) {
            activityIds.push(id + '#multiInstanceBody');
          }
          angular.forEach(instanceList, function(instance) {
            activityInstanceIds.push(instance.id);
          });
        } else if (idx !== -1) {
          activityIds.splice(idx, 1);
          if (multiInstance) {
            activityIds.splice(
              activityIds.indexOf(id + '#multiInstanceBody'),
              1
            );
          }

          angular.forEach(instanceList, function(instance) {
            var instanceId = instance.id,
              index = activityInstanceIds.indexOf(instanceId);

            if (index !== -1) {
              activityInstanceIds.splice(index, 1);
            }
          });
        }
      }

      newFilter = {
        activityIds: activityIds,
        activityInstanceIds: activityInstanceIds
      };

      processData.set('filter', newFilter);
    };

    $scope.handleActivityInstanceSelection = function(id, activityId, $event) {
      if (!id) {
        processData.set('filter', {});
        return;
      }

      var ctrlKey = $event.ctrlKey,
        activityIds = angular.copy($scope.filter.activityIds) || [],
        activityInstanceIds =
          angular.copy($scope.filter.activityInstanceIds) || [],
        idx = activityInstanceIds.indexOf(id),
        instanceList = $scope.activityIdToInstancesMap[activityId],
        newFilter;

      if (!ctrlKey) {
        activityIds = [activityId];
        activityInstanceIds = [id];
      } else if (ctrlKey) {
        if (idx === -1) {
          activityInstanceIds.push(id);

          var index = activityIds.indexOf(activityId);
          if (index === -1) {
            activityIds.push(activityId);
          }
        } else {
          activityInstanceIds.splice(idx, 1);

          var foundAnotherActivityInstance = false;
          if (instanceList) {
            for (var i = 0, instance; (instance = instanceList[i]); i++) {
              var instanceId = instance.id,
                instanceIndex = activityInstanceIds.indexOf(instanceId);

              if (instanceIndex !== -1) {
                foundAnotherActivityInstance = true;
              }
            }
          }

          if (!foundAnotherActivityInstance) {
            var otherIndex = activityIds.indexOf(activityId);
            activityIds.splice(otherIndex, 1);
          }
        }
      }

      newFilter = {
        activityIds: activityIds,
        activityInstanceIds: activityInstanceIds,
        scrollToBpmnElement: activityId
      };

      processData.set('filter', newFilter);
    };

    $scope.orderChildrenBy = function() {
      return function(elem) {
        var id = elem.id,
          idx = id.indexOf(':');

        return idx !== -1 ? id.substr(idx + 1, id.length) : id;
      };
    };

    $scope.$on('$routeChangeStart', function() {
      page.breadcrumbsClear();
    });

    $scope.processInstanceVars = {
      read: ['processInstance', 'processData', 'filter', 'pageData']
    };

    $scope.processInstanceActions = Views.getProviders({
      component: 'cockpit.processInstance.runtime.action'
    });

    $scope.callbacks = {
      handleRootChange: function(selection, canvas) {
        var newFilter = angular.copy($scope.filter);

        newFilter.activityIds = selection || [];

        newFilter.activityInstanceIds = $scope.filter.activityInstanceIds.filter(
          instanceId => {
            var instance = $scope.instanceIdToInstanceMap[instanceId];
            return (
              canvas.getRootElement() === canvas.findRoot(instance.activityId)
            );
          }
        );

        processData.set('filter', newFilter);
      }
    };

    Data.instantiateProviders('cockpit.processInstance.data', {
      $scope: $scope,
      processData: processData
    });

    // INITIALIZE PLUGINS
    var instancePlugins = Views.getProviders({
      component: 'cockpit.processInstance.runtime.tab'
    })
      .concat(
        Views.getProviders({
          component: 'cockpit.processInstance.runtime.action'
        })
      )
      .concat(Views.getProviders({component: 'cockpit.processInstance.view'}))
      .concat(
        Views.getProviders({
          component: 'cockpit.processInstance.diagram.overlay'
        })
      );

    var initData = {
      processInstance: processInstance,
      processData: processData,
      filter: $scope.filter,
      pageData: pageData
    };

    $scope.initData = initData;

    for (var i = 0; i < instancePlugins.length; i++) {
      if (typeof instancePlugins[i].initialize === 'function') {
        instancePlugins[i].initialize(initData);
      }
    }
  }
];

ngModule.controller('ProcessInstanceFilterController', [
  '$scope',
  '$translate',
  function($scope, $translate) {
    var processData = $scope.processData.newChild($scope),
      filterData;

    processData.provide('filterData', [
      'filter',
      function(filter) {
        if (!filterData || filterData.filter != filter) {
          var activityIds = filter.activityIds || [],
            activityInstanceIds = filter.activityInstanceIds || [];

          return {
            filter: filter,
            activityCount: activityIds.length || 0,
            activityInstanceCount: activityInstanceIds.length || 0
          };
        } else {
          return filterData;
        }
      }
    ]);

    processData.observe(['filterData'], function(_filterData) {
      $scope.filterData = filterData = _filterData;
    });

    $scope.clearSelection = function() {
      // update cached filter
      filterData = {
        activityCount: 0,
        activityInstanceCount: 0,
        filter: {}
      };

      processData.set('filter', filterData.filter);
    };

    $scope.getDataWhen = function() {
      return {
        null: $translate.instant('PAGES_PROCESS_INSTANCES_NOTHING'),
        0: $translate.instant('PAGES_PROCESS_INSTANCES_NOTHING'),
        one: $translate.instant('PAGES_PROCESS_INSTANCES_ONE_SELECT'),
        other: $translate.instant('PAGES_PROCESS_INSTANCES_OTHER_SELECT')
      };
    };

    $scope.sidebarTab = 'info';
  }
]);

var RouteConfig = [
  '$routeProvider',
  function($routeProvider) {
    $routeProvider.when('/process-instance/:id', {
      redirectTo: routeUtil.redirectToRuntime,
      reloadOnSearch: false
    });

    $routeProvider.when('/process-instance/:id/runtime', {
      template: template,
      controller: Controller,
      authentication: 'required',
      resolve: {
        processInstance: [
          'ResourceResolver',
          'ProcessInstanceResource',
          'Uri',
          'Views',
          'Notifications',
          '$route',
          '$http',
          '$location',
          '$translate',
          function(
            ResourceResolver,
            ProcessInstanceResource,
            Uri,
            Views,
            Notifications,
            $route,
            $http,
            $location,
            $translate
          ) {
            return ResourceResolver.getByRouteParam('id', {
              name: $translate.instant(
                'PROCESS_INSTANCE_RUNNING_PROCESS_INSTANCE'
              ),

              resolve: function(id) {
                return ProcessInstanceResource.get({id: id});
              },

              redirectTo: function() {
                var id = $route.current.params['id'];

                $http
                  .get(
                    Uri.appUri(
                      'engine://engine/:engine/history/process-instance/'
                    ) + id
                  )
                  .then(function(result) {
                    var path;
                    var search;

                    var status = $translate.instant(
                      'PROCESS_INSTANCE_STATUS_UNABLE_DISPLAY_RUNNING_INSTANCE'
                    );
                    var message = $translate.instant(
                      'PROCESS_INSTANCE_MESSAGE_2',
                      {id: id}
                    );

                    var historyProvider = Views.getProvider({
                      id: 'history',
                      component: 'cockpit.processInstance.view'
                    });

                    if (historyProvider) {
                      // keep search params
                      search = $location.search();
                      path = '/process-instance/' + id + '/history';

                      message =
                        message +
                        $translate.instant('PROCESS_INSTANCE_MESSAGE_3');
                    } else {
                      path =
                        '/process-definition/' +
                        result.data.processDefinitionId;

                      message =
                        message +
                        $translate.instant('PROCESS_INSTANCE_MESSAGE_4');
                    }

                    $location.path(path);
                    $location.search(search || {});
                    $location.replace();

                    Notifications.addMessage({
                      status: status,
                      message: message,
                      http: true,
                      exclusive: ['http'],
                      duration: 5000
                    });
                  })
                  .catch(function() {
                    $location.path('/dashboard');
                    $location.search({});
                    $location.replace();

                    Notifications.addError({
                      status: $translate.instant(
                        'PROCESS_INSTANCE_STATUS_FAILED_RUNNING_PROCESS'
                      ),
                      message: $translate.instant(
                        'PROCESS_INSTANCE_MESSAGE_5',
                        {id: id}
                      ),
                      http: true,
                      exclusive: ['http']
                    });
                  });
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
    ViewsProvider.registerDefaultView('cockpit.processInstance.view', {
      id: 'runtime',
      priority: 20,
      label: 'BREAD_CRUMBS_RUNTIME',
      keepSearchParams: ['viewbox']
    });
  }
];

ngModule.config(RouteConfig).config(ViewConfig);

module.exports = ngModule;
