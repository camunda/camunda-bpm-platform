ngDefine('cockpit.pages.processInstance', [
  'require',
  'module:dataDepend:angular-data-depend'
], function(module, require) {

  function ProcessInstanceController ($scope, $rootScope, $location, $filter, $dialog, search, ProcessDefinitionResource, ProcessInstanceResource, IncidentResource, Views, Transform, processInstance, dataDepend) {
    
    $rootScope.clearBreadcrumbs();

    $scope.processInstance = processInstance;

    var currentFilter;
    var controllerInitialized = false;
    
    var processData = $scope.processData = dataDepend.create($scope);

    // utilities ///////////////////////

    $scope.$on('$routeChanged', function() {
      var filter = completeFilter(parseFilterFromUri(), $scope.instanceIdToInstanceMap, $scope.activityIdToInstancesMap);
      processData.set('filter', filter);
    });

    function collect(elements, fn) {
      var result = [];

      angular.forEach(elements, function(e) {
        try {
          var c = fn(e);

          if (c !== undefined) {
            result.push(c);
          }
        } catch (e) {
          ; // safe collect -> error skips element
        }
      });

      return result;
    }

    var currentFilter = null;

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

      function parseVariables(vars) {
        return collect(vars, Variables.parse);
      }

      currentFilter = {
        activityIds: activityIdsParam,
        activityInstanceIds: activityInstanceIdsParam,
        page: parseInt(params.page) || undefined
      };

      $scope.filter = currentFilter;

      return currentFilter;
    }

    function serializeFilterToUri(filter) {
      var activityIds = filter.activityIds,
          activityInstanceIds = filter.activityInstanceIds;

      function nonEmpty(array) {
        return array && array.length;
      }

      search.updateSilently({
        activityIds: nonEmpty(activityIds) ? activityIds.join(',') : null,
        activityInstanceIds: nonEmpty(activityInstanceIds) ? activityInstanceIds.join(',') : null
      });

      $scope.filter = currentFilter = filter;
    }

    function completeFilter(filter, instanceIdToInstanceMap, activityIdToInstancesMap) {
      var activityIds = angular.copy(filter.activityIds) || [],
          activityInstanceIds = angular.copy(filter.activityInstanceIds) || [],
          page = parseInt(filter.page) || undefined,
          scrollToBpmnElement;

      angular.forEach(activityInstanceIds, function (instanceId) {
        var instance = instanceIdToInstanceMap[instanceId],
            activityId = instance.activityId || instance.targetActivityId,
            idx = activityIds.indexOf(activityId);

        if (idx === -1) {
          activityIds.push(activityId);
        }
      });

      angular.forEach(activityIds, function (activityId) {
        var instanceList = activityIdToInstancesMap[activityId],
            foundAtLeastOne = false,
            instanceIds = [];

        if (instanceList) {

          for (var i = 0, instance; !!(instance = instanceList[i]); i++) {
            var idx = activityInstanceIds.indexOf(instance.id);

            if (idx !== -1) {
              foundAtLeastOne = true;
              break;
            }

            instanceIds.push(instance.id);
          }

          if (!foundAtLeastOne) {
            activityInstanceIds = activityInstanceIds.concat(instanceIds);
          }

        }
      });

      if (activityIds.length > 0) {
        scrollToBpmnElement = activityIds[activityIds.length-1];
      }

      filter = {};

      filter['activityIds'] = activityIds;
      filter['activityInstanceIds'] = activityInstanceIds;
      filter['scrollToBpmnElement'] = scrollToBpmnElement;
      filter['page'] = page;

      $scope.filter = currentFilter = filter;

      return filter;
    }

    // end utilities ///////////////////////

    // /////// Begin definition of process data 

    // processInstance
    processData.provide('processInstance', processInstance);

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
          name = bpmnElement.type + ' (' + shortenFilter(bpmnElement.id, 8) + '...)';
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

          for (var i = 0, transition; !!(transition = transitions[i]); i++) {
            var activityId = transition.targetActivityId,
                bpmnElement = bpmnElements[activityId],
                instances = activityIdToInstancesMap[activityId] || [];

            transition.name = getActivityName(bpmnElement);
            activityIdToInstancesMap[activityId] = instances;
            if(!instanceIdToInstanceMap[transition.id]) {
              instanceIdToInstanceMap[transition.id] = transition;
            }
            instances.push(transition);
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
          for (var i = 0, executionId; !!(executionId = executionIds[i]); i++) {
            executionIdToInstanceMap[executionId] = instance;
          }
        }

        if (executionId) {
          executionIdToInstanceMap[executionId] = instance;
        }
      }

      return executionIdToInstanceMap;
    }]);

    // activityInstanceStatistics, clickableElements
    processData.provide([ 'activityInstanceStatistics', 'clickableElements'], [ 'activityIdToInstancesMap', function (activityIdToInstancesMap) {
      var activityInstanceStatistics = [],
          clickableElements = [];

      for (var activityId in activityIdToInstancesMap) {
        var instances = activityIdToInstancesMap[activityId];
        activityInstanceStatistics.push( {id: activityId, count: instances.length });
        clickableElements.push(activityId);
      }

      return [ activityInstanceStatistics, clickableElements ];
    }]);

    // incidents
    processData.provide('incidents', ['processInstance', function (processInstance) {
      return IncidentResource.query({ id : processInstance.id }).$promise;
    }]);

    // incidentStatistics
    processData.provide('incidentStatistics', ['incidents', function (incidents) {
      var incidentStatistics = [],
          statistics = {};

      for (var i = 0, incident; !!(incident = incidents[i]); i++) {
        var activity = statistics[incident.activityId];
        if (!activity) {
          activity = [];
          statistics[incident.activityId] = activity;
        }
        activity.push(incident);
      }

      for (var key in statistics) {
        var tmp = {};
        tmp.id = key;
        tmp.incidents = statistics[key];
        incidentStatistics.push(tmp);
      }

      return incidentStatistics;
    }]);

    // processDiagram
    processData.provide('processDiagram', [ 'semantic', 'processDefinition', 'bpmnElements', function (semantic, processDefinition, bpmnElements) {
      var processDiagram = {};

      processDiagram.semantic = semantic;
      processDiagram.processDefinition = processDefinition;
      processDiagram.bpmnElements = bpmnElements;

      return processDiagram;
    }]);

    processData.provide('filter', parseFilterFromUri());

    // /////// End definition of process data 


    // /////// Begin usage of definied process data 

    processData.observe([ 'filter', 'instanceIdToInstanceMap', 'activityIdToInstancesMap'], function (filter, instanceIdToInstanceMap, activityIdToInstancesMap) {
      if (!controllerInitialized) {
        filter = completeFilter(filter, instanceIdToInstanceMap, activityIdToInstancesMap)
        processData.set('filter', filter);

        controllerInitialized = true;
      }
    });

    processData.observe('filter',  function(filter) {
      if (filter != currentFilter) {
        serializeFilterToUri(filter);
        $scope.filter = filter;
      }
    });

    $scope.processDefinition = processData.observe('processDefinition', function (processDefinition) {
      $scope.processDefinition = processDefinition;
    });

    processData.observe([ 'processDefinition', 'processInstance'], function (processDefinition, processInstance) {
      $rootScope.addBreadcrumb({'type': 'processDefinition', 'processDefinition': processDefinition});
      $rootScope.addBreadcrumb({'type': 'processInstance', 'processInstance': processInstance,'processDefinition': processDefinition});
    });

    $scope.activityInstanceTree = processData.observe('activityInstanceTree', function (activityInstanceTree) {
      $scope.activityInstanceTree = activityInstanceTree;
    });

    $scope.processDiagram = processData.observe('processDiagram', function (processDiagram) {
      $scope.processDiagram = processDiagram;
    });

    $scope.processDiagramOverlay = processData.observe([ 'processDiagram', 'activityInstanceStatistics', 'clickableElements', 'incidentStatistics' ], function (processDiagram, activityInstanceStatistics, clickableElements, incidentStatistics) {
      $scope.processDiagramOverlay.annotations = activityInstanceStatistics;
      $scope.processDiagramOverlay.incidents = incidentStatistics;
      $scope.processDiagramOverlay.clickableElements = clickableElements;
      $scope.processDiagramOverlay = angular.extend({}, $scope.processDiagramOverlay);
    });

    processData.observe([ 'instanceIdToInstanceMap', 'activityIdToInstancesMap' ], function (instanceIdToInstanceMap, activityIdToInstancesMap) {
      $scope.instanceIdToInstanceMap = instanceIdToInstanceMap;
      $scope.activityIdToInstancesMap = activityIdToInstancesMap;
    });

    // /////// End of usage of definied process data 

    $scope.handleBpmnElementSelection = function (id, $event) {
      if (!id) {
        var filter = {activityIds: null, activityInstanceIds: null};
        processData.set('filter', filter);
        return;
      }

      var ctrlKey = $event.ctrlKey,
          activityIds = angular.copy(currentFilter.activityIds) || [],
          activityInstanceIds = angular.copy(currentFilter.activityInstanceIds) || [],
          idx = activityIds.indexOf(id),
          instanceList = $scope.activityIdToInstancesMap[id],
          filter = {};

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

      filter['activityIds'] = activityIds;
      filter['activityInstanceIds'] = activityInstanceIds;

      processData.set('filter', filter);
    }; 

    $scope.handleActivityInstanceSelection = function (id, activityId, $event) {
      if (!id) {
        var filter = {activityIds: null, activityInstanceIds: null};
        processData.set('filter', filter);
        return;
      }

      var ctrlKey = $event.ctrlKey,
          activityIds = angular.copy(currentFilter.activityIds) || [],
          activityInstanceIds = angular.copy(currentFilter.activityInstanceIds) || [],
          idx = activityInstanceIds.indexOf(id),
          instanceList = $scope.activityIdToInstancesMap[activityId],
          filter = {};

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
        } else

        if (idx !== -1) {
          activityInstanceIds.splice(idx, 1);

          var foundAnotherActivityInstance = false;
          if (instanceList) {
            for (var i = 0, instance; !!(instance = instanceList[i]); i++) {
              var instanceId = instance.id,
                  index = activityInstanceIds.indexOf(instanceId);

              if (index !== -1) {
                foundAnotherActivityInstance = true;
              }
            }
          }

          if (!foundAnotherActivityInstance) {
            var index = activityIds.indexOf(activityId);
            activityIds.splice(index, 1);
          }         
        }
      }

      filter['activityIds'] = activityIds;
      filter['activityInstanceIds'] = activityInstanceIds;
      filter['scrollToBpmnElement'] = activityId;

      processData.set('filter', filter);
    };

    function createDialog(options) {

      var resolve = angular.extend(options.resolve || {}, {
        processData: function() { return $scope.processData; },
        processInstance: function() { return $scope.processInstance; }
      });

      options.resolve = resolve;

      return $dialog.dialog(options);
    }

    $scope.openCancelProcessInstanceDialog = function () {
      var dialog = createDialog({
        controller: 'CancelProcessInstanceController',
        templateUrl: require.toUrl('./cancel-process-instance.html')
      });

      dialog.open().then(function(result) {

        // dialog closed. YEA!
      });
    };

    $scope.openJobRetriesDialog = function () {
      var dialog = createDialog({
        controller: 'JobRetriesController',
        templateUrl: require.toUrl('./set-job-retries.html')
      });

      dialog.open().then(function(result) {

        // dialog closed. YEA!
      });
    };
    
    $scope.openAddVariableDialog = function () {
      var dialog = createDialog({
        controller: 'AddVariableController',
        templateUrl: require.toUrl('./add-variable.html')
      });

      dialog.open().then(function(result) {
        if (result === "SUCCESS") {
          // refresh filter and all views
          processData.set('filter', angular.extend({}, $scope.filter));
        }
      });
    };

    $scope.$on('$routeChangeStart', function () {
      $rootScope.clearBreadcrumbs();
    });

    $scope.processInstanceVars = { read: [ 'processInstance', 'processData' ] };
    $scope.processInstanceTabs = Views.getProviders({ component: 'cockpit.processInstance.instanceDetails' });

    $scope.selectView = function(view) {
      $scope.selectedView = view;

      search.updateSilently({
        detailsTab: view.id
      });
    };

    function setDefaultTab(tabs) {
      var selectedTabId = search().detailsTab;

      if (!tabs.length) {
        return;
      }

      if (selectedTabId) {
        var provider = Views.getProvider({ component: 'cockpit.processInstance.instanceDetails', id: selectedTabId });
        if (provider && tabs.indexOf(provider) != -1) {
          $scope.selectedView = provider;
          return;
        }
      }

      search.updateSilently({
        detailsTab: null
      });

      $scope.selectedView = tabs[0];
    }

    setDefaultTab($scope.processInstanceTabs);

  };

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

  };

  module
    .controller('ProcessInstanceController', [ '$scope',
                                               '$rootScope',
                                               '$location',
                                               '$filter',
                                               '$dialog',
                                               'search',
                                               'ProcessDefinitionResource',
                                               'ProcessInstanceResource',
                                               'IncidentResource',
                                               'Views',
                                               'Transform',
                                               'processInstance',
                                               'dataDepend', ProcessInstanceController ])
    .controller('ProcessInstanceFilterController', ['$scope', ProcessInstanceFilterController]);

  var RouteConfig = [ '$routeProvider', 'AuthenticationServiceProvider', function($routeProvider, AuthenticationServiceProvider) {
    $routeProvider.when('/process-instance/:processInstanceId', {
      templateUrl: 'pages/process-instance.html',
      controller: 'ProcessInstanceController',
      resolve: {
        authenticatedUser: AuthenticationServiceProvider.requireAuthenticatedUser,
        processInstance: ['ResourceResolver', 'ProcessInstanceResource',
          function(ResourceResolver, ProcessInstanceResource) {
            return ResourceResolver.getByRouteParam('processInstanceId', {
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

  module.config(RouteConfig);
});
