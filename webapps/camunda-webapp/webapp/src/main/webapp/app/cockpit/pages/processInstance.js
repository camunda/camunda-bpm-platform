ngDefine('cockpit.pages.processInstance', [
  'module:dataDepend:angular-data-depend'
], function(module) {

  function ProcessInstanceController ($scope, $rootScope, $routeParams, $location, $q, $filter, search, ProcessDefinitionResource, ProcessInstanceResource, IncidentResource, Views, Transform, processInstance, dataDependFactory) {
    
    $scope.processInstance = processInstance;

    $scope.cancelProcessInstanceDialog = new Dialog();
    $scope.cancelProcessInstanceDialog.setAutoClosable(false);

    $scope.jobRetriesDialog = new Dialog();
    $scope.jobRetriesDialog.setAutoClosable(false);

    var currentFilter;
    
    var processData = $scope.processData = dataDependFactory.create($scope);

    // utilities ///////////////////////

    $scope.$on('$routeChanged', function() {
      processData.set('filter', parseFilterFromUri());
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
          activityInstancesParam = parseArray(params.activityInstances),
          bpmnElementsParam = parseArray(params.bpmnElements);

      function parseArray(str) {
        if (!str) {
          return [];
        }

        return str.split(/,/);
      }

      function parseVariables(vars) {
        return collect(vars, Variables.parse);
      }

      angular.forEach(activityInstancesParam, function (instanceParam) {
        var instance = instanceIdToInstanceMap[instanceParam],
            activityId = instance.activityId || targetActivityId,
            idx = bpmnElementsParam.indexOf(activityId);

        if (idx === -1) {
          bpmnElementsParam.push(activityId);
        }
      });

      angular.forEach(bpmnElementsParam, function (bpmnElementParam) {
        var instanceList = activityIdToInstancesMap[bpmnElementParam],
            foundAtLeastOne = false,
            instanceIds = [];

        for (var i = 0, instance; !!(instance = instanceList[i]); i++) {
          var idx = activityInstancesParam.indexOf(instance.id);

          if (idx !== -1) {
            foundAtLeastOne = true;
            break;
          }

          instanceIds.push(instance.id);
        }

        if (!foundAtLeastOne) {
          activityInstancesParam = activityInstancesParam.concat(instanceIds);
        }
      });


      currentFilter = {
        activityIds: bpmnElementsParam,
        activityInstanceIds: activityInstancesParam,
        page: parseInt(params.page) || undefined
      };

      return currentFilter;
    }

    function serializeFilterToUri(filter) {
      var activityIds = filter.activityIds,
          activityInstanceIds = filter.activityInstanceIds;

      function nonEmpty(array) {
        return array && array.length;
      }

      search.updateSilently({
        bpmnElements: nonEmpty(activityIds) ? activityIds.join(',') : null,
        activityInstances: nonEmpty(activityInstanceIds) ? activityInstanceIds.join(',') : null
      });

      currentFilter = filter;
    }

    // end utilities ///////////////////////

    // /////// Begin definition of process data 

    // processInstance
    processData.set('processInstance', processInstance);

    // processDefinition
    processData.set('processDefinition', ['processInstance', function (processInstance) {
      var deferred = $q.defer();

      ProcessDefinitionResource.get({ id: processInstance.definitionId }).$then(function(response) {
        deferred.resolve(response.data);
      });

      return deferred.promise;
    }]);

    // semantic
    processData.set('semantic', ['processDefinition', function (processDefinition) {
      var deferred = $q.defer();
      
      ProcessDefinitionResource.getBpmn20Xml({ id: processDefinition.id }).$then(function(response) {
        deferred.resolve(Transform.transformBpmn20Xml(response.data.bpmn20Xml));
      });
      
      return deferred.promise;
    }]);

    // bpmnElements
    processData.set('bpmnElements', ['semantic', 'processDefinition', function (semantic, processDefinition) {
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
    processData.set('activityInstances', ['processInstance', function (processInstance) {
      var deferred = $q.defer();
      
      ProcessInstanceResource.activityInstances({ id: processInstance.id }).$then(function(response) {
        deferred.resolve(response.data);
      });
      
      return deferred.promise;
    }]);

    // activityInstanceTree, activityIdToInstancesMap, instanceIdToInstanceMap
    processData.set([ 'activityInstanceTree', 'activityIdToInstancesMap', 'instanceIdToInstanceMap' ], 
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
            instanceIdToInstanceMap[child.id] = child;            
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
            instanceIdToInstanceMap[transition.id] = transition;     
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

    // activityInstanceStatistics, clickableElements
    processData.set([ 'activityInstanceStatistics', 'clickableElements'], [ 'activityIdToInstancesMap', function (activityIdToInstancesMap) {
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
    processData.set('incidents', ['processInstance', function (processInstance) {
      var deferred = $q.defer();
      
      IncidentResource.query({ id : processInstance.id }).$then(function(response) {
        deferred.resolve(response.data);
      });
      
      return deferred.promise;
    }]);

    // incidentStatistics
    processData.set('incidentStatistics', ['incidents', function (incidents) {
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
    processData.set('processDiagram', [ 'semantic', 'processDefinition', 'bpmnElements', function (semantic, processDefinition, bpmnElements) {
      var processDiagram = {};

      processDiagram.semantic = semantic;
      processDiagram.processDefinition = processDefinition;
      processDiagram.bpmnElements = bpmnElements;

      return processDiagram;
    }]);

    processData.set('filter', parseFilterFromUri());

    // /////// End definition of process data 


    // /////// Begin usage of definied process data 

    processData.get('filter', function(filter) {
      if (filter != currentFilter) {
        console.log('filter changed -> ', filter);
        
        serializeFilterToUri(filter);
      }
    });

    processData.get([ 'processDefinition', 'processInstance'], function (processDefinition, processInstance) {
      $rootScope.addBreadcrumb({'type': 'processDefinition', 'processDefinition': processDefinition});
      $rootScope.addBreadcrumb({'type': 'processInstance', 'processInstance': processInstance,'processDefinition': processDefinition});
    });

    $scope.activityInstanceTree = processData.get('activityInstanceTree', function (activityInstanceTree) {
      $scope.activityInstanceTree = activityInstanceTree;
    });

    $scope.processDiagram = processData.get('processDiagram', function (processDiagram) {
      $scope.processDiagram = processDiagram;
    });

    $scope.processDiagramOverlay = processData.get([ 'processDiagram', 'activityInstanceStatistics', 'clickableElements', 'incidentStatistics' ], function (processDiagram, activityInstanceStatistics, clickableElements, incidentStatistics) {
      $scope.processDiagramOverlay.annotations = activityInstanceStatistics;
      $scope.processDiagramOverlay.incidents = incidentStatistics;
      $scope.processDiagramOverlay.clickableElements = clickableElements;
      $scope.processDiagramOverlay = angular.extend({}, $scope.processDiagramOverlay);
    });

    processData.get([ 'instanceIdToInstanceMap', 'activityIdToInstancesMap' ], function (instanceIdToInstanceMap, activityIdToInstancesMap) {

      $scope.instanceIdToInstanceMap = instanceIdToInstanceMap;
      $scope.activityIdToInstancesMap = activityIdToInstancesMap;

      initializeFilter(instanceIdToInstanceMap, activityIdToInstancesMap);
    });

    // /////// End of usage of definied process data 

    $scope.handleBpmnElementSelection = function (id, $event) {
      if (!id) {
        currentFilter = {activityIds: null, activityInstanceIds: null};
        processData.set('filter', currentFilter);
        return;
      }

      var ctrlKey = $event.ctrlKey,
          activityIds = angular.copy($scope.filter.activityIds) || [],
          activityInstanceIds = angular.copy($scope.filter.activityInstanceIds) || [],
          idx = activityIds.indexOf(id),
          instanceList = $scope.activityIdToInstancesMap[id];

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

      currentFilter = {};

      currentFilter['activityIds'] = activityIds;
      currentFilter['activityInstanceIds'] = activityInstanceIds;

      processData.set('filter', currentFilter);
    }; 

    $scope.handleActivityInstanceSelection = function (id, activityId, $event) {
      if (!id) {
        currentFilter = {activityIds: null, activityInstanceIds: null};
        processData.set('filter', currentFilter);
        return;
      }

      var ctrlKey = $event.ctrlKey,
          activityIds = angular.copy($scope.filter.activityIds) || [],
          activityInstanceIds = angular.copy($scope.filter.activityInstanceIds) || [],
          idx = activityInstanceIds.indexOf(id),
          instanceList = $scope.activityIdToInstancesMap[activityId];

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
          for (var i = 0, instance; !!(instance = instanceList[i]); i++) {
            var instanceId = instance.id,
                index = activityInstanceIds.indexOf(instanceId);

            if (index !== -1) {
              foundAnotherActivityInstance = true;
            }
          }

          if (!foundAnotherActivityInstance) {
            var index = activityIds.indexOf(activityId);
            activityIds.splice(index, 1);
          }         
        }
      }

      currentFilter = {};

      currentFilter['activityIds'] = activityIds;
      currentFilter['activityInstanceIds'] = activityInstanceIds;
      currentFilter['scrollToBpmnElement'] = activityId;

      processData.set('filter', currentFilter);
    };

    function initializeFilter (instanceIdToInstanceMap, activityIdToInstancesMap) {
      var activityInstancesParam = $location.search().activityInstances || [], 
          bpmnElementsParam = $location.search().bpmnElements || [];

      if (angular.isString(activityInstancesParam)) {
        activityInstancesParam = activityInstancesParam.split(',');
      }
      
      if (angular.isString(bpmnElementsParam)) {
        bpmnElementsParam = bpmnElementsParam.split(',');
      }

      angular.forEach(activityInstancesParam, function (instanceParam) {
        var instance = instanceIdToInstanceMap[instanceParam],
            activityId = instance.activityId || targetActivityId,
            idx = bpmnElementsParam.indexOf(activityId);

        if (idx === -1) {
          bpmnElementsParam.push(activityId);
        }
      });

      angular.forEach(bpmnElementsParam, function (bpmnElementParam) {
        var instanceList = activityIdToInstancesMap[bpmnElementParam],
            foundAtLeastOne = false,
            instanceIds = [];

        for (var i = 0, instance; !!(instance = instanceList[i]); i++) {
          var idx = activityInstancesParam.indexOf(instance.id);

          if (idx !== -1) {
            foundAtLeastOne = true;
            break;
          }

          instanceIds.push(instance.id);
        }

        if (!foundAtLeastOne) {
          activityInstancesParam = activityInstancesParam.concat(instanceIds);
        }
      });


      currentFilter = {};

      currentFilter['activityIds'] = bpmnElementsParam;
      currentFilter['activityInstanceIds'] = activityInstancesParam;
      currentFilter['scrollToBpmnElement'] = bpmnElementsParam[bpmnElementsParam.length-1];

      processData.set('filter', currentFilter);
    }

    $scope.openCancelProcessInstanceDialog = function () {
      $scope.cancelProcessInstanceDialog.open();      
    };

    $scope.openJobRetriesDialog = function () {
      $scope.jobRetriesDialog.open();      
    };
    
    $scope.$on('$routeChangeStart', function () {
      $rootScope.clearBreadcrumbs();
    });

    $scope.processInstanceVars = { read: [ 'processInstance', 'selection', 'processData', 'updateLocation' ] };
    $scope.processInstanceViews = Views.getProviders({ component: 'cockpit.processInstance.instanceDetails' });

    $scope.selectView = function(view) {
      $scope.selectedView = view;
      $location.search('detailsTab', view.id);
    };

    function setDefaultTab(tabs) {
      var selectedTabId = $location.search().detailsTab;

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

      $location.search('detailsTab', null);
      $scope.selectedView = tabs[0];
    }

    setDefaultTab($scope.processInstanceViews);

  };

  module.controller('ProcessInstanceController', [ '$scope', '$rootScope','$routeParams', '$location', '$q', '$filter', 'search', 'ProcessDefinitionResource', 'ProcessInstanceResource', 'IncidentResource', 'Views', 'Transform', 'processInstance', 'dataDependFactory', ProcessInstanceController ]);

  var SearchFactory = [ '$location', '$rootScope', function($location, $rootScope) {

    var silent = false;

    $rootScope.$on('$routeUpdate', function(e, lastRoute) {
      if (silent) {
        console.log('silenced $routeUpdate');
        silent = false;
      } else {
        $rootScope.$broadcast('$routeChanged', lastRoute);
      }
    });

    $rootScope.$on('$routeChangeSuccess', function(e, lastRoute) {
      silent = false;
    });

    var search = function() {
      var args = Array.prototype.slice(arguments);

      return $location.search.apply($location, arguments);
    }

    search.updateSilently = function(params) {
      angular.forEach(params, function(value, key) {
        $location.search(key, value);
      });

      silent = true;
    };

    return search;
  }];

  module.factory('search', SearchFactory);

  var RouteConfig = function ($routeProvider) {
    $routeProvider.when('/process-definition/:processDefinitionId/process-instance/:processInstanceId', {
      templateUrl: 'pages/process-instance.html',
      controller: 'ProcessInstanceController',
      resolve: {
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
  };

  RouteConfig.$inject = ['$routeProvider'];

  module
    .config(RouteConfig);
});
