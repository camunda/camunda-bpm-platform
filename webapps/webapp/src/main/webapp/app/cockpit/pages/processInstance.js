ngDefine('cockpit.pages.processInstance', [
  'module:dataDepend:angular-data-depend'
], function(module) {

  function ProcessInstanceController ($scope, $rootScope, $routeParams, $location, $q, $filter, ProcessDefinitionResource, ProcessInstanceResource, IncidentResource, Views, Transform, processInstance, dataDependFactory) {
    
    $scope.processInstance = processInstance;

    $scope.cancelProcessInstanceDialog = new Dialog();
    $scope.cancelProcessInstanceDialog.setAutoClosable(false);

    $scope.jobRetriesDialog = new Dialog();
    $scope.jobRetriesDialog.setAutoClosable(false);

    // flag to be used on '$routeUpdate' event.
    var internalUpdateLocation = false;
    
    var processData = $scope.processData = dataDependFactory.create($scope);

    var updateLocation = $scope.updateLocation = function (fn) {
      internalUpdateLocation = true;
      fn($location);
    };

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
      var processDiagram = $scope.processDiagram = $scope.processDiagram || {};

      processDiagram.semantic = semantic;
      processDiagram.processDefinition = processDefinition;
      processDiagram.bpmnElements = bpmnElements;

      return processDiagram;
    }]);

    processData.set('filter', {});

    // /////// End definition of process data 


    // /////// Begin usage of definied process data 

    $scope.filter = processData.get('filter', function (filter) {
      $scope.filter = filter;

      // var activityIds = filter.activityIds,
      //     activityInstanceIds = filter.activityInstanceIds;

        // updateLocation(function (location) {
        //   if (!activityIds || activityIds.length === 0) {
        //     location.search('bpmnElements', null);
        //   } else {
        //     location.search('bpmnElements', activityIds);
        //   }

        //   if (!activityInstanceIds || activityInstanceIds.length === 0) {
        //     location.search('activityInstances', null);
        //   } else {
        //     location.search('activityInstances', activityInstanceIds);
        //   }        

      // }
    });

    processData.get([ 'processDefinition', 'processInstance'], function (processDefinition, processInstance) {
      $rootScope.addBreadcrumb({'type': 'processDefinition', 'processDefinition': processDefinition});
      $rootScope.addBreadcrumb({'type': 'processInstance', 'processInstance': processInstance,'processDefinition': processDefinition});
    });

    $scope.activityInstanceTree = processData.get('activityInstanceTree', function (activityInstanceTree) {
      $scope.activityInstanceTree = activityInstanceTree;
    });

    $scope.processDiagramOverlay = processData.get([ 'processDiagram', 'activityInstanceStatistics', 'clickableElements', 'incidentStatistics' ], function (processDiagram, activityInstanceStatistics, clickableElements, incidentStatistics) {
      $scope.processDiagramOverlay.annotations = activityInstanceStatistics;
      $scope.processDiagramOverlay.incidents = incidentStatistics;
      $scope.processDiagramOverlay.clickableElements = clickableElements;
      $scope.processDiagramOverlay = angular.extend({}, $scope.processDiagramOverlay);
    });

    processData.get([ 'instanceIdToInstanceMap', 'bpmnElements', 'activityIdToInstancesMap' ], function (instanceIdToInstanceMap, bpmnElements, activityIdToInstancesMap) {

      $scope.instanceIdToInstanceMap = instanceIdToInstanceMap;
      $scope.bpmnElements = bpmnElements;
      $scope.activityIdToInstancesMap = activityIdToInstancesMap;

      // initialize filter
      // var activityInstancesSearchParam = $location.search().activityInstances || [],
      //     bpmnElementsSearchParam = $location.search().bpmnElements || [],
      //     page = $location.search().page || 1;

      // initializeSelection(activityInstancesSearchParam, bpmnElementsSearchParam, page, instanceIdToInstanceMap, bpmnElements, activityIdToInstancesMap);

    });

    $scope.activityIdToInstancesMap = processData.get('activityIdToInstancesMap', function (activityIdToInstancesMap) {
      $scope.activityIdToInstancesMap = angular.extend($scope.activityIdToInstancesMap, activityIdToInstancesMap);
    });

    $scope.bpmnElements = processData.get('bpmnElements', function (bpmnElements) {
      $scope.bpmnElements = angular.extend($scope.bpmnElements, bpmnElements);
    });

    $scope.instanceIdToInstanceMap = processData.get('instanceIdToInstanceMap', function (instanceIdToInstanceMap) {
      $scope.instanceIdToInstanceMap = angular.extend($scope.instanceIdToInstanceMap, instanceIdToInstanceMap);

    });

    // /////// End of usage of definied process data 

    $scope.handleBpmnElementSelection = function (id, $event) {
      if (!id) {
        processData.set('filter', {activityIds: null, activityInstanceIds: null});
        return;
      }

      var ctrlKey = $event.ctrlKey,
          activityIds = angular.copy($scope.filter.activityIds) || [],
          activityInstanceIds = angular.copy($scope.filter.activityInstanceIds) || [],
          idx = activityIds.indexOf(id),
          filter = {},
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

      filter['activityIds'] = activityIds;
      filter['activityInstanceIds'] = activityInstanceIds;

      processData.set('filter', filter);
    }; 

    $scope.handleActivityInstanceSelection = function (id, activityId, $event) {
      if (!id) {
        processData.set('filter', {activityIds: null, activityInstanceIds: null});
        return;
      }

      var ctrlKey = $event.ctrlKey,
          activityIds = angular.copy($scope.filter.activityIds) || [],
          activityInstanceIds = angular.copy($scope.filter.activityInstanceIds) || [],
          idx = activityInstanceIds.indexOf(id),
          filter = {},
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

      filter['activityIds'] = activityIds;
      filter['activityInstanceIds'] = activityInstanceIds;
      filter['scrollToBpmnElement'] = activityId;

      processData.set('filter', filter);
    };  

    // function initializeSelection(activityInstancesSearchParam, bpmnElementsSearchParam, page, instanceIdToInstanceMap, bpmnElements, activityIdToInstancesMap) {
    //   if (angular.isString(activityInstancesSearchParam)) {
    //     activityInstancesSearchParam = activityInstancesSearchParam.split(',');
    //   }
      
    //   if (angular.isString(bpmnElementsSearchParam)) {
    //     bpmnElementsSearchParam = bpmnElementsSearchParam.split(',');
    //   }

    //   // add corresponding activity instances to the array of inside the selection object
    //   var activityInstances = $scope.selection.view['activityInstances'] = [];
    //   angular.forEach(activityInstancesSearchParam, function (instanceId) {
    //     var instance = instanceIdToInstanceMap[instanceId];
    //     if (instance) {
    //       activityInstances.push(instance);  

    //       // It can happen, that the corresponding bpmn element is not provided
    //       // as id in the bpmn elements search parameter. So in that case, the 
    //       // bpmn element id has to be added to the search parameter.
    //       var activityId = instance.activityId || instance.targetActivityId;
    //       var idx = bpmnElementsSearchParam.indexOf(activityId);
    //       if (idx === -1) {
    //         bpmnElementsSearchParam.push(activityId);
    //       }
    //     }
    //   });

    //   // add corresponding activity instances to the array of inside the selection object
    //   var selectedBpmnElements = $scope.selection.view['bpmnElements'] = [];
    //   angular.forEach(bpmnElementsSearchParam, function (bpmnElementId) {
    //     var bpmnElement = bpmnElements[bpmnElementId];
    //     if (bpmnElement) {
    //       selectedBpmnElements.push(bpmnElement);  

    //       var addCompleteInstanceList = true;
    //       var instanceList = activityIdToInstancesMap[bpmnElementId];
    //       if (instanceList && instanceList.length > 0) {
    //         // find at least one activity instance id for the bpmn element.
    //         // if at least one has been found, than the instanceList is not
    //         // allowed to be added to the selection.
    //         for (var i = 0, instance; !!(instance = instanceList[i]); i++) {
    //           var idx = activityInstancesSearchParam.indexOf(instance.id);
    //           if (idx !== -1) {
    //             addCompleteInstanceList = false;
    //             break;
    //           }
    //         }
    //         if (addCompleteInstanceList) {
    //           for (var i = 0, instance; !!(instance = instanceList[i]); i++) {
    //             activityInstances.push(instance);
    //             activityInstancesSearchParam.push(instance.id);
    //           }
    //         }
    //       }
    //     }
    //   });

    //   if (selectedBpmnElements.length > 0) {
    //     $scope.selection.view.scrollToBpmnElement = selectedBpmnElements[selectedBpmnElements.length-1];
    //   }

    //   updateLocation(function (location) {
    //     if (activityInstancesSearchParam.length === 0) {
    //       $location.search('activityInstances', null);
    //     } else {
    //       $location.search('activityInstances', activityInstancesSearchParam);
    //     }

    //     if (bpmnElementsSearchParam.length === 0) {
    //       $location.search('bpmnElements', null);
    //     } else {
    //       $location.search('bpmnElements', bpmnElementsSearchParam);
    //     }
    //   });

    //   var filter = {};

    //   filter['activityInstances'] = activityInstancesSearchParam;
    //   filter['bpmnElements'] = bpmnElementsSearchParam;
    //   filter['page'] = page;

    //   processData.set('filter', filter);
    // }

    // $scope.$on('$routeUpdate', function () {
    //   if (internalUpdateLocation) {
    //     internalUpdateLocation = false;
    //   } else {
    //     var activityInstancesSearchParam = $location.search().activityInstances || [],
    //         bpmnElementsSearchParam = $location.search().bpmnElements || [],
    //         page = 1;

    //     processData.get([ 'instanceIdToInstanceMap', 'bpmnElements', 'activityIdToInstancesMap' ], function (instanceIdToInstanceMap, bpmnElements, activityIdToInstancesMap) {
    //       initializeSelection(activityInstancesSearchParam, bpmnElementsSearchParam, page, instanceIdToInstanceMap, bpmnElements, activityIdToInstancesMap);
    //       updateLocation(function (location) {
    //         location.search('page', null);
    //       });
          
    //     });

    //   }
    // });

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

  module.controller('ProcessInstanceController', [ '$scope', '$rootScope','$routeParams', '$location', '$q', '$filter', 'ProcessDefinitionResource', 'ProcessInstanceResource', 'IncidentResource', 'Views', 'Transform', 'processInstance', 'dataDependFactory', ProcessInstanceController ]);

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
