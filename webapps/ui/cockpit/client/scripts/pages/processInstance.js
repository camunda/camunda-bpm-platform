'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/process-instance.html', 'utf8');

var angular = require('camunda-commons-ui/vendor/angular');
var commonModule = require('../../../../common/scripts/module');

var routeUtil = require('../../../../common/scripts/util/routeUtil');
var camCommons = require('camunda-commons-ui/lib');

var ngModule = angular.module('cam.cockpit.pages.processInstance',
  [
    camCommons.name,
    commonModule.name,
    'dataDepend'
  ]
);

var Controller = [
  '$scope', '$filter', '$rootScope', '$location', 'search', 'ProcessDefinitionResource', 'ProcessInstanceResource',
  'IncidentResource', 'Views', 'Data', 'Transform', 'processInstance', 'dataDepend', 'page', 'breadcrumbTrails',
  'integrateActivityInstanceFilter', 'isModuleAvailable',
  function($scope, $filter, $rootScope, $location, search, ProcessDefinitionResource, ProcessInstanceResource,
      IncidentResource, Views, Data, Transform, processInstance,   dataDepend, page, breadcrumbTrails,
      integrateActivityInstanceFilter, isModuleAvailable) {

    $scope.hasMigrationPlugin = isModuleAvailable('cockpit.plugin.migration');
    $scope.processInstance = processInstance;

    var processData = $scope.processData = dataDepend.create($scope);
    var pageData = $scope.pageData = dataDepend.create($scope);

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
    processData.provide('processDefinition', ['processInstance', function(processInstance) {
      return ProcessDefinitionResource.get({ id: processInstance.definitionId }).$promise;
    }]);

    processData.provide('bpmn20Xml', [ 'processDefinition', function(definition) {
      return ProcessDefinitionResource.getBpmn20Xml({ id : definition.id}).$promise;
    }]);

    // bpmnElements
    processData.provide('parsedBpmn20', [ 'bpmn20Xml', function(bpmn20Xml) {
      return Transform.transformBpmn20Xml(bpmn20Xml.bpmn20Xml);
    }]);

    processData.provide('bpmnElements', [ 'parsedBpmn20', function(parsedBpmn20) {
      return parsedBpmn20.bpmnElements;
    }]);

    processData.provide('bpmnDefinition', [ 'parsedBpmn20', function(parsedBpmn20) {
      return parsedBpmn20.definitions;
    }]);

    // activityInstances
    processData.provide('activityInstances', ['processInstance', function(processInstance) {
      return ProcessInstanceResource.activityInstances({ id: processInstance.id }).$promise;
    }]);

    // activityInstanceTree, activityIdToInstancesMap, instanceIdToInstanceMap
    processData.provide([ 'activityInstanceTree', 'activityIdToInstancesMap', 'instanceIdToInstanceMap' ], [
      'activityInstances', 'processDefinition', 'bpmnElements',
      function(activityInstances,   processDefinition,   bpmnElements) {
        var activityIdToInstancesMap = {},
            instanceIdToInstanceMap = {},
            model = bpmnElements[processDefinition.key];

        function getActivityName(bpmnElement) {
          var name = bpmnElement.name;
          if (!name) {
            var shortenFilter = $filter('shorten');
            name = bpmnElement.$type.substr(5) + ' (' + shortenFilter(bpmnElement.id, 8) + ')';
          }

          return name;
        }

        function decorateActivityInstanceTree(instance) {
          var children = instance.childActivityInstances;


          if (children && children.length > 0) {

            for (var i = 0, child; (child = children[i]); i++) {
              var activityId = child.activityId,
                  bpmnElement = bpmnElements[activityId],
                  instances = activityIdToInstancesMap[activityId] || [];

              if(bpmnElement) {
                child.name = getActivityName(bpmnElement);
              } else {
                child.name = activityId;
              }
              child.isTransitionInstance = false;
              activityIdToInstancesMap[activityId] = instances;
              if(!instanceIdToInstanceMap[child.id]) {
                instanceIdToInstanceMap[child.id] = child;
              }
              instances.push(child);

              decorateActivityInstanceTree(child);
            }
          }

          var transitions = instance.childTransitionInstances;
          if (transitions && transitions.length > 0) {

            for (var t = 0, transition; (transition = transitions[t]); t++) {
              var targetActivityId = transition.targetActivityId,
                  transitionBpmnElement = bpmnElements[targetActivityId],
                  transitionInstances = activityIdToInstancesMap[targetActivityId] || [];

              if(transitionBpmnElement) {
                transition.name = getActivityName(transitionBpmnElement);
              } else {
                transition.name = targetActivityId;
              }
              transition.isTransitionInstance = true;
              activityIdToInstancesMap[targetActivityId] = transitionInstances;
              if(!instanceIdToInstanceMap[transition.id]) {
                instanceIdToInstanceMap[transition.id] = transition;
              }
              transitionInstances.push(transition);
            }
          }
        }

        activityInstances.name = getActivityName(model);

        // add initially the root to the map
        instanceIdToInstanceMap[activityInstances.id] = activityInstances;

        decorateActivityInstanceTree(activityInstances);

        return [ activityInstances, activityIdToInstancesMap, instanceIdToInstanceMap ];
      }]);

    processData.provide('executionIdToInstanceMap', ['instanceIdToInstanceMap', function(instanceIdToInstanceMap) {
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
    }]);

    // incidents
    processData.provide('incidents', ['processInstance', function(processInstance) {
      return IncidentResource.query({ processInstanceId : processInstance.id }).$promise;
    }]);

    // incidentStatistics
    processData.provide('activityIdToIncidentsMap', ['incidents', function(incidents) {
      var activityIdToIncidentsMap = {};

      for (var i = 0, incident; (incident = incidents[i]); i++) {
        var activity = activityIdToIncidentsMap[incident.activityId];
        if (!activity) {
          activity = [];
          activityIdToIncidentsMap[incident.activityId] = activity;
        }
        activity.push(incident);
      }

      return activityIdToIncidentsMap;
    }]);

    // processDiagram
    processData.provide('processDiagram', [
      'bpmnDefinition', 'bpmnElements',
      function(bpmnDefinition,   bpmnElements) {
        var processDiagram = {};

        processDiagram.bpmnDefinition = bpmnDefinition;
        processDiagram.bpmnElements = bpmnElements;

        return processDiagram;
      }]);

    processData.provide('latestDefinition', ['processDefinition', function(definition) {
      var queryParams = {
        'key' : definition.key,
        'sortBy': 'version',
        'sortOrder': 'desc',
        'latestVersion': true };

      if(definition.tenantId) {
        queryParams.tenantIdIn = [ definition.tenantId ];
      } else {
        queryParams.withoutTenantId = true;
      }

      return ProcessDefinitionResource.query(queryParams).$promise;
    }]);

    // /////// End definition of process data
    integrateActivityInstanceFilter($scope, angular.noop, {
      shouldRemoveActivityIds: true
    });

    // /////// Begin usage of definied process data

    $scope.processDefinition = processData.observe('processDefinition', function(processDefinition) {
      $scope.processDefinition = processDefinition;
    });

    $scope.latestProcessDefinition = processData.observe('latestDefinition', function(processDefinition) {
      $scope.latestProcessDefinition = processDefinition[0];
    });

    $scope.isLatestVersion = function() {
      return $scope.processDefinition.version === $scope.latestProcessDefinition.version;
    };

    $scope.getMigrationUrl = function() {
      var path = '#/migration';

      var searches = {
        sourceKey: $scope.processDefinition.key,
        targetKey: $scope.latestProcessDefinition.key,
        sourceVersion: $scope.processDefinition.version,
        targetVersion: $scope.latestProcessDefinition.version,
        searchQuery: JSON.stringify([{
          type     : 'processInstanceIds',
          operator : 'eq',
          value    : $scope.processInstance.id
        }])
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
        deploymentsQuery: JSON.stringify([{
          type     : 'id',
          operator : 'eq',
          value    : deploymentId
        }])
      };

      return routeUtil.redirectTo(path, searches, [ 'deployment', 'resourceName', 'deploymentsQuery' ]);
    };

    processData.provide('superProcessInstanceCount', ['processInstance', function(processInstance) {
      return ProcessInstanceResource.count({ subProcessInstance : processInstance.id }).$promise;
    }]);

    function fetchSuperProcessInstance(processInstance, done) {

      ProcessInstanceResource.query({
        subProcessInstance: processInstance.id
      })
      .$promise.then(function(response) {

        // var superInstance = response.data[0];
        var superInstance = response[0];

        done(null, superInstance);
      });
    }

    $rootScope.showBreadcrumbs = true;

    processData.observe([
      'processDefinition', 'processInstance', 'superProcessInstanceCount'],
    function(processDefinition,   processInstance,   superProcessInstanceCount) {
      var crumbs = [
        {
          label: 'Processes',
          href: '#/processes/'
        }
      ];

      if (superProcessInstanceCount.count) {
        crumbs.push(function(index) {
          breadcrumbTrails(processInstance, fetchSuperProcessInstance, [], index, 'runtime');
        });
      }

      crumbs.push({
        label: processDefinition.name || processDefinition.key || processDefinition.id,
        href: '#/process-definition/'+ (processDefinition.id) +'/runtime',
        keepSearchParams : [ 'viewbox' ]
      });


      var plugins = Views.getProviders({ component: 'cockpit.processInstance.view' });

      crumbs.push({
        type: 'processInstance',
        divider: ':',
        label: processInstance.name || processInstance.key || processInstance.id,
        href: '#/process-instance/'+ (processInstance.id) +'/runtime',
        processInstance: processInstance,

        choices: plugins.sort(function(a, b) {
          return a.priority < b.priority ? -1 : (a.priority > b.priority ? 1 : 0);
        }).map(function(plugin) {
          return {
            active: plugin.id === 'runtime',
            label: plugin.label,
            href: '#/process-instance/' + processInstance.id + '/' + plugin.id
          };
        })
      });

      page
        .breadcrumbsClear()
        .breadcrumbsAdd(crumbs);

      page.titleSet([
        $scope.processDefinition.name || $scope.processDefinition.id,
        'Instance View'
      ].join(' | '));
    });

    $scope.activityInstanceTree = processData.observe('activityInstanceTree', function(activityInstanceTree) {
      $scope.activityInstanceTree = activityInstanceTree;
    });

    $scope.processDiagram = processData.observe('processDiagram', function(processDiagram) {
      $scope.processDiagram = processDiagram;
    });

    processData.observe([ 'instanceIdToInstanceMap', 'activityIdToInstancesMap' ], function(instanceIdToInstanceMap, activityIdToInstancesMap) {
      $scope.instanceIdToInstanceMap = instanceIdToInstanceMap;
      $scope.activityIdToInstancesMap = activityIdToInstancesMap;
    });

    // /////// End of usage of definied process data

    $scope.handleBpmnElementSelection = function(id, $event) {
      if (!id) {
        processData.set('filter', {});
        return;
      }

      var ctrlKey = $event.ctrlKey,
          activityIds = angular.copy($scope.filter.activityIds) || [],
          activityInstanceIds = angular.copy($scope.filter.activityInstanceIds) || [],
          idx = activityIds.indexOf(id),
          instanceList = angular.copy($scope.activityIdToInstancesMap[id]) || [],
          multiInstance = $scope.activityIdToInstancesMap[id+'#multiInstanceBody'],
          newFilter;

      if(multiInstance) {
        Array.prototype.push.apply(instanceList, multiInstance);
        if(idx === -1) {
          idx = activityIds.indexOf(id+'#multiInstanceBody');
        }
      }

      if (!ctrlKey) {
        activityIds = [ id ];
        if(multiInstance) {
          activityIds.push( id+'#multiInstanceBody' );
        }

        activityInstanceIds = [];
        angular.forEach(instanceList, function(instance) {
          activityInstanceIds.push(instance.id);
        });
      } else

      if (ctrlKey) {

        if (idx === -1) {
          activityIds.push(id);
          if(multiInstance) {
            activityIds.push( id+'#multiInstanceBody' );
          }
          angular.forEach(instanceList, function(instance) {
            activityInstanceIds.push(instance.id);
          });
        } else

        if (idx !== -1) {
          activityIds.splice(idx, 1);
          if(multiInstance) {
            activityIds.splice( activityIds.indexOf(id+'#multiInstanceBody' ), 1);
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
          activityInstanceIds = angular.copy($scope.filter.activityInstanceIds) || [],
          idx = activityInstanceIds.indexOf(id),
          instanceList = $scope.activityIdToInstancesMap[activityId],
          newFilter;

      if (!ctrlKey) {
        activityIds = [ activityId ];
        activityInstanceIds = [ id ];
      } else

      if (ctrlKey) {

        if (idx === -1) {

          activityInstanceIds.push(id);

          var index = activityIds.indexOf(activityId);
          if (index === -1) {
            activityIds.push(activityId);
          }
        }
        else {
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

    $scope.processInstanceVars = { read: [ 'processInstance', 'processData', 'filter', 'pageData' ] };

    $scope.processInstanceActions = Views.getProviders({ component: 'cockpit.processInstance.runtime.action' });

    Data.instantiateProviders('cockpit.processInstance.data', {$scope: $scope, processData : processData});

    // INITIALIZE PLUGINS
    var instancePlugins = (
        Views.getProviders({ component: 'cockpit.processInstance.runtime.tab' })).concat(
        Views.getProviders({ component: 'cockpit.processInstance.runtime.action' })).concat(
        Views.getProviders({ component: 'cockpit.processInstance.view' })).concat(
        Views.getProviders({ component: 'cockpit.processInstance.diagram.overlay' }));

    var initData = {
      processInstance : processInstance,
      processData     : processData,
      filter          : $scope.filter,
      pageData        : pageData
    };

    $scope.initData = initData;

    for(var i = 0; i < instancePlugins.length; i++) {
      if(typeof instancePlugins[i].initialize === 'function') {
        instancePlugins[i].initialize(initData);
      }
    }
  }];

ngModule
    .controller('ProcessInstanceFilterController', [
      '$scope',
      function($scope) {
        var processData = $scope.processData.newChild($scope),
            filterData;

        processData.provide('filterData', [ 'filter', function(filter) {

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
        }]);

        processData.observe([ 'filterData' ], function(_filterData) {
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

        $scope.sidebarTab = 'info';
      }]);

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
          'ResourceResolver', 'ProcessInstanceResource', 'Uri', 'Views', 'Notifications', '$route', '$http', '$location',
          function(ResourceResolver,   ProcessInstanceResource,   Uri,   Views,   Notifications,   $route,   $http,   $location) {

            return ResourceResolver.getByRouteParam('id', {
              name: 'running process instance',

              resolve: function(id) {
                return ProcessInstanceResource.get({ id : id });
              },

              redirectTo: function() {
                var id = $route.current.params['id'];

                $http.get(Uri.appUri('engine://engine/:engine/history/process-instance/') + id)
                .success (function(result) {

                  var path;
                  var search;

                  var status = 'Unable to display running process instance';
                  var message = 'Process instance with ID ' + id + ' has been completed. Redirecting to ';

                  var historyProvider = Views.getProvider({
                    id: 'history',
                    component: 'cockpit.processInstance.view'
                  });

                  if (historyProvider) {
                    // keep search params
                    search = $location.search();
                    path = '/process-instance/' + id + '/history';

                    message = message + 'historic process instance view.';
                  }
                  else {
                    path = '/process-definition/' + result.processDefinitionId;

                    message = message + 'process definition view.';
                  }

                  $location.path(path);
                  $location.search(search || {});
                  $location.replace();

                  Notifications.addMessage({
                    status: status,
                    message: message,
                    http: true,
                    exclusive: [ 'http' ],
                    duration: 5000
                  });

                })
                .error (function() {

                  $location.path('/dashboard');
                  $location.search({});
                  $location.replace();

                  Notifications.addError({
                    status: 'Failed to display running process instance',
                    message: 'No running process instance with ID ' + id,
                    http: true,
                    exclusive: [ 'http' ]
                  });
                });
              }
            });
          }]
      },
      reloadOnSearch: false
    });
  }];

var ViewConfig = [ 'ViewsProvider', function(ViewsProvider) {
  ViewsProvider.registerDefaultView('cockpit.processInstance.view', {
    id: 'runtime',
    priority: 20,
    label: 'Runtime',
    keepSearchParams: [ 'viewbox' ]
  });
}];

ngModule
    .config(RouteConfig)
    .config(ViewConfig)
  ;

module.exports = ngModule;
