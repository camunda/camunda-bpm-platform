/* global ngDefine: false */
ngDefine('cockpit.pages.processInstance', [
  'require',
  'angular',
  'cockpit/util/routeUtil',
  'module:dataDepend:angular-data-depend'
], function(module, require, angular) {
  'use strict';

  var routeUtil = require('cockpit/util/routeUtil');

  var Controller = [
    '$scope',
    '$filter',
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
  function(
    $scope,
    $filter,
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
    breadcrumbTrails
  ) {

    $scope.processInstance = processInstance;

    var filter;

    var processData = $scope.processData = dataDepend.create($scope);

    // utilities ///////////////////////

    $scope.$on('$routeChanged', function() {
      processData.set('filter', parseFilterFromUri());
      // update tab selection
      setDefaultTab($scope.processInstanceTabs);
    });

    // function collect(elements, fn) {
    //   var result = [];

    //   angular.forEach(elements, function(e) {
    //     try {
    //       var c = fn(e);

    //       if (c !== undefined) {
    //         result.push(c);
    //       }
    //     } catch (ex) {
    //       // safe collect -> error skips element
    //     }
    //   });

    //   return result;
    // }

    function parseFilterFromUri() {

      var params = search(),
          activityInstanceIdsParam = parseArray(params.activityInstanceIds),
          activityIdsParam = parseArray(params.activityIds);

      function parseArray(str) {
        if (!str) {
          return [];
        }

        return str.split(/,/);
      }

      // function parseVariables(vars) {
      //   return collect(vars, Variables.parse);
      // }

      $scope.filter = filter = {
        activityIds: activityIdsParam,
        activityInstanceIds: activityInstanceIdsParam,
        page: parseInt(params.page, 10) || undefined
      };

      return filter;
    }

    function serializeFilterToUri(newFilter) {
      var activityIds = newFilter.activityIds,
          activityInstanceIds = newFilter.activityInstanceIds;

      function nonEmpty(array) {
        return array && array.length;
      }

      search.updateSilently({
        activityIds: nonEmpty(activityIds) ? activityIds.join(',') : null,
        activityInstanceIds: nonEmpty(activityInstanceIds) ? activityInstanceIds.join(',') : null
      });

      $scope.filter = filter = newFilter;
    }

    /**
     * Auto complete a filter based on the given filter data.
     *
     * It performs the following logic
     *
     *   - If activity instances are selected, select the associated activities unless they are explicitly specified.
     *   - If an activity is selected, select the associated activity instances unless they are explicitly specified.
     *
     * @param  {Object} filter the filter to auto complete
     * @param  {Object} instanceIdToInstanceMap a activity instance id -> activity instance map
     * @param  {*} activityIdToInstancesMap a activity id -> activity instance map
     */
    function autoCompleteFilter(newFilter, instanceIdToInstanceMap, activityIdToInstancesMap) {

      var activityIds = newFilter.activityIds || [],
          activityInstanceIds = newFilter.activityInstanceIds || [],
          page = parseInt(newFilter.page, 10) || null,
          scrollToBpmnElement = newFilter.scrollToBpmnElement,
          // if filter has been changed from outside this component,
          // newFilter is different from cached filter
          externalUpdate = newFilter !== filter,
          changed,
          completedFilter;

      angular.forEach(activityInstanceIds, function (instanceId) {
        var instance = instanceIdToInstanceMap[instanceId] || {},
            activityId = instance.activityId || instance.targetActivityId,
            idx = activityIds.indexOf(activityId);

        if (idx === -1) {
          activityIds.push(activityId);
          changed = true;
        }
      });

      angular.forEach(activityIds, function (activityId) {
        var instanceList = activityIdToInstancesMap[activityId],
            foundOne = false,
            instanceIds = [];

        if (instanceList) {

          for (var i = 0, instance; !!(instance = instanceList[i]); i++) {
            var idx = activityInstanceIds.indexOf(instance.id);

            if (idx !== -1) {
              foundOne = true;
              break;
            }

            instanceIds.push(instance.id);
          }

          if (!foundOne) {
            activityInstanceIds = activityInstanceIds.concat(instanceIds);
            changed = true;
          }

        }
      });

      if (activityIds.length > 0) {
        var newScrollTo = activityIds[activityIds.length - 1];

        if (newScrollTo !== scrollToBpmnElement) {
          scrollToBpmnElement = newScrollTo;
          changed = true;
        }
      }

      completedFilter = {
        activityIds: activityIds,
        activityInstanceIds: activityInstanceIds,
        scrollToBpmnElement: scrollToBpmnElement,
        page: page
      };

      changed = !angular.equals(completedFilter, newFilter);

      // update filter only if actual changes happened above
      // (auto completion took place)
      if (changed) {

        // update cached filters
        $scope.filter = filter = completedFilter;

        // notify external components of filter change
        processData.set('filter', filter);
      }

      // update uri only if filter change is triggered from
      // external view component
      if (externalUpdate) {

        // serialize filter to url
        serializeFilterToUri(filter);
      }
    }

    // end utilities ///////////////////////

    // /////// Begin definition of process data

    // processInstance
    processData.provide('processInstance', processInstance);

    // filter
    processData.provide('filter', parseFilterFromUri());

    // processDefinition
    processData.provide('processDefinition', ['processInstance', function (processInstance) {
      return ProcessDefinitionResource.get({ id: processInstance.definitionId }).$promise;
    }]);

    // semantic
    processData.provide('semantic', ['processDefinition', function (processDefinition) {
      return ProcessDefinitionResource.getBpmn20Xml({ id: processDefinition.id }).$promise.then(function(response) {
        return Transform.transformBpmn20Xml(response.bpmn20Xml);
      });
    }]);

    // bpmnElements
    processData.provide('bpmnElements', ['semantic', 'processDefinition', function (semantic, processDefinition) {
      var bpmnElements = {},
          key = processDefinition.key,
          model = null;

      for (var i = 0, currentSemantic; !!(currentSemantic = semantic[i]); i++) {
        var id = currentSemantic.id,
            type = currentSemantic.type;

        if (id === key && type === 'process') {
          model = currentSemantic;
          break;
        }
      }

      function collectBpmnElements(element, result) {
        result[element.id] = element;

        var baseElements = element.baseElements;
        if (baseElements && baseElements.length > 0) {
          for (var i = 0, child; !!(child = baseElements[i]); i++) {
            collectBpmnElements(child, result);
          }
        }
      }

      collectBpmnElements(model, bpmnElements);

      return bpmnElements;

    }]);

    // activityInstances
    processData.provide('activityInstances', ['processInstance', function (processInstance) {
      return ProcessInstanceResource.activityInstances({ id: processInstance.id }).$promise;
    }]);

    // activityInstanceTree, activityIdToInstancesMap, instanceIdToInstanceMap
    processData.provide([ 'activityInstanceTree', 'activityIdToInstancesMap', 'instanceIdToInstanceMap' ],
      [ 'activityInstances',
        'processDefinition',
        'bpmnElements', function (activityInstances, processDefinition, bpmnElements) {
      var activityIdToInstancesMap = {},
          instanceIdToInstanceMap = {},
          model = bpmnElements[processDefinition.key];

      function getActivityName(bpmnElement) {
        var name = bpmnElement.name;
        if (!name) {
          var shortenFilter = $filter('shorten');
          name = bpmnElement.type + ' (' + shortenFilter(bpmnElement.id, 8) + ')';
        }

        return name;
      }

      function decorateActivityInstanceTree(instance) {
        var children = instance.childActivityInstances;


        if (children && children.length > 0) {

          for (var i = 0, child; !!(child = children[i]); i++) {
            var activityId = child.activityId,
                bpmnElement = bpmnElements[activityId],
                instances = activityIdToInstancesMap[activityId] || [];


            child.name = getActivityName(bpmnElement);
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

          for (var t = 0, transition; !!(transition = transitions[t]); t++) {
            var targetActivityId = transition.targetActivityId,
                transitionBpmnElement = bpmnElements[targetActivityId],
                transitionInstances = activityIdToInstancesMap[targetActivityId] || [];

            transition.name = getActivityName(transitionBpmnElement);
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

    processData.provide('executionIdToInstanceMap', ['instanceIdToInstanceMap', function (instanceIdToInstanceMap) {
      var executionIdToInstanceMap = {};

      for (var key in instanceIdToInstanceMap) {
        var instance = instanceIdToInstanceMap[key],
            executionIds = instance.executionIds,
            executionId = instance.executionId;

        if (executionIds) {
          for (var i = 0, execId; !!(execId = executionIds[i]); i++) {
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
    processData.provide('incidents', ['processInstance', function (processInstance) {
      return IncidentResource.query({ processInstanceId : processInstance.id }).$promise;
    }]);

    // incidentStatistics
    processData.provide('activityIdToIncidentsMap', ['incidents', function (incidents) {
      var activityIdToIncidentsMap = {};

      for (var i = 0, incident; !!(incident = incidents[i]); i++) {
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
    processData.provide('processDiagram', [ 'semantic', 'processDefinition', 'bpmnElements', function (semantic, processDefinition, bpmnElements) {
      var processDiagram = {};

      processDiagram.semantic = semantic;
      processDiagram.processDefinition = processDefinition;
      processDiagram.bpmnElements = bpmnElements;

      return processDiagram;
    }]);

    // /////// End definition of process data


    // /////// Begin usage of definied process data

    processData.observe([ 'filter', 'instanceIdToInstanceMap', 'activityIdToInstancesMap'], autoCompleteFilter);

    $scope.processDefinition = processData.observe('processDefinition', function (processDefinition) {
      $scope.processDefinition = processDefinition;
    });

    processData.provide('superProcessInstanceCount', ['processInstance', function (processInstance) {
      return ProcessInstanceResource.count({ subProcessInstance : processInstance.id }).$promise;
    }]);

    function fetchSuperProcessInstance(processInstance, done) {

      ProcessInstanceResource.query({
        subProcessInstance: processInstance.id
      })
      .$then(function(response) {

        var superInstance = response.data[0];

        done(null, superInstance);
      });
    }

    processData.observe([
      'processDefinition',
      'processInstance',
      'superProcessInstanceCount'
    ], function (processDefinition, processInstance, superProcessInstanceCount) {
      var crumbs = [];

      if (superProcessInstanceCount.count) {
        crumbs.push(function(index) {
          breadcrumbTrails(processInstance, fetchSuperProcessInstance, [], index, 'runtime');
        });
      }

      crumbs.push({
        label: processDefinition.name || ((processDefinition.key || processDefinition.id).slice(0, 8) +'…'),
        href: '#/process-definition/'+ (processDefinition.id) +'/runtime'
      });
      crumbs.push({
        divider: ':',
        label: processInstance.name || ((processInstance.key || processInstance.id).slice(0, 8) +'…'),
        href: '#/process-instance/'+ (processInstance.id) +'/runtime'
      });

      page
        .breadcrumbsClear()
        .breadcrumbsAdd(crumbs);

      page.titleSet([
        'camunda Cockpit',
        $scope.processDefinition.name || $scope.processDefinition.id,
        'Instance View'
      ].join(' | '));
    });

    $scope.activityInstanceTree = processData.observe('activityInstanceTree', function (activityInstanceTree) {
      $scope.activityInstanceTree = activityInstanceTree;
    });

    $scope.processDiagram = processData.observe('processDiagram', function (processDiagram) {
      $scope.processDiagram = processDiagram;
    });

    processData.observe([ 'instanceIdToInstanceMap', 'activityIdToInstancesMap' ], function (instanceIdToInstanceMap, activityIdToInstancesMap) {
      $scope.instanceIdToInstanceMap = instanceIdToInstanceMap;
      $scope.activityIdToInstancesMap = activityIdToInstancesMap;
    });

    // /////// End of usage of definied process data

    $scope.handleBpmnElementSelection = function (id, $event) {

      if (!id) {
        processData.set('filter', {});
        return;
      }

      var ctrlKey = $event.ctrlKey,
          activityIds = angular.copy(filter.activityIds) || [],
          activityInstanceIds = angular.copy(filter.activityInstanceIds) || [],
          idx = activityIds.indexOf(id),
          instanceList = $scope.activityIdToInstancesMap[id],
          newFilter;

      if (!ctrlKey) {
        activityIds = [ id ];
        activityInstanceIds = [];
        angular.forEach(instanceList, function (instance) {
          activityInstanceIds.push(instance.id);
        });
      } else

      if (ctrlKey) {

        if (idx === -1) {
          activityIds.push(id);
          angular.forEach(instanceList, function (instance) {
            activityInstanceIds.push(instance.id);
          });
        } else

        if (idx !== -1) {
          activityIds.splice(idx, 1);

          angular.forEach(instanceList, function (instance) {
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

    $scope.handleActivityInstanceSelection = function (id, activityId, $event) {

      if (!id) {
        processData.set('filter', {});
        return;
      }

      var ctrlKey = $event.ctrlKey,
          activityIds = angular.copy(filter.activityIds) || [],
          activityInstanceIds = angular.copy(filter.activityInstanceIds) || [],
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
            for (var i = 0, instance; !!(instance = instanceList[i]); i++) {
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

    $scope.$on('$routeChangeStart', function () {
      page.breadcrumbsClear();
    });

    $scope.processInstanceVars = { read: [ 'processInstance', 'processData', 'filter' ] };
    $scope.processInstanceTabs = Views.getProviders({ component: 'cockpit.processInstance.runtime.tab' });

    $scope.processInstanceActions = Views.getProviders({ component: 'cockpit.processInstance.runtime.action' });

    Data.instantiateProviders('cockpit.processInstance.data', {$scope: $scope, processData : processData});

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
        var provider = Views.getProvider({ component: 'cockpit.processInstance.runtime.tab', id: selectedTabId });

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

    setDefaultTab($scope.processInstanceTabs);
  }];

  function ProcessInstanceFilterController ($scope) {

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

    $scope.clearSelection = function () {
      // update cached filter
      filterData = {
        activityCount: 0,
        activityInstanceCount: 0,
        filter: {}
      };

      processData.set('filter', filterData.filter);
    };

  }

  module
    .controller('ProcessInstanceFilterController', ['$scope', ProcessInstanceFilterController]);

  var RouteConfig = [ '$routeProvider', 'AuthenticationServiceProvider', function($routeProvider, AuthenticationServiceProvider) {

    $routeProvider.when('/process-instance/:id', {
      redirectTo: routeUtil.redirectToRuntime
    });

    $routeProvider.when('/process-instance/:id/runtime', {
      templateUrl: require.toUrl('./pages/process-instance.html'),
      controller: Controller,
      resolve: {
        authenticatedUser: AuthenticationServiceProvider.requireAuthenticatedUser,
        processInstance: ['ResourceResolver', 'ProcessInstanceResource',
          function(ResourceResolver, ProcessInstanceResource) {
            return ResourceResolver.getByRouteParam('id', {
              name: 'process instance',
              resolve: function(id) {
                return ProcessInstanceResource.get({ id : id });
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
      label: 'Runtime'
    });
  }];

  module
    .config(RouteConfig)
    .config(ViewConfig);
});
