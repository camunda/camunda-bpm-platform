ngDefine('cockpit.pages', function(module) {

  function ProcessInstanceController ($scope, $routeParams, $location, $q, $filter, ProcessDefinitionResource, ProcessInstanceResource, IncidentResource, Views, Transform) {
    
    $scope.processDefinitionId = $routeParams.processDefinitionId;
    $scope.processInstanceId = $routeParams.processInstanceId;

    $scope.selection = {};
    
    $scope.$watch('selection.treeDiagramMapping', function (newValue) {
      if (!newValue) {
        return;
      }

      if (newValue.scrollTo) {
        var bpmnElement = $scope.processInstance.activityIdToBpmnElementMap[newValue.scrollTo.activityId];
        $scope.selection.treeDiagramMapping.scrollTo = null;
        $scope.selection.treeDiagramMapping.scrollToBpmnElement = bpmnElement;
      }
      
      if (newValue.activityInstances) {
        $scope.selection.treeDiagramMapping.bpmnElements = [];
        angular.forEach(newValue.activityInstances, function(activityInstance) {
          var activityId = activityInstance.activityId || activityInstance.targetActivityId;
          var bpmnElement = $scope.processInstance.activityIdToBpmnElementMap[activityId];
          $scope.selection.treeDiagramMapping.bpmnElements.push(bpmnElement);
        });
        return;
      }
      
      if (newValue.bpmnElements) {
        $scope.selection.treeDiagramMapping.activityInstances = [];
        angular.forEach(newValue.bpmnElements, function(bpmnElement) {
          var instanceList = $scope.processInstance.activityIdToInstancesMap[bpmnElement.id];
          $scope.selection.treeDiagramMapping.activityInstances = $scope.selection.treeDiagramMapping.activityInstances.concat(instanceList);
        });
        return;
      }
      
    });
    
    $scope.$watch('selection.elements', function (newValue) {
      if (!newValue) {
        return;
      }
      
      if (newValue.hidden) {
        var elements = [];
        if (newValue.hidden === 'sidebar') {
          elements.push('main-content');
        }
        $scope.selection.elements.toResize = {toGreater: elements}; 
        return;
      };

      if (newValue.visible) {
        var elements = [];
        if (newValue.visible === 'sidebar') {
          elements.push('main-content');
        }
        $scope.selection.elements.toResize = {toShrink: elements}; 
        return;
      };
    });
    
    $scope.processInstance = {};
    
    // get the process definition
    function loadProcessDefinition() {
      var deferred = $q.defer();
      
      ProcessDefinitionResource.get({ id: $scope.processDefinitionId }).$then(function(response) {
        deferred.resolve(response.data);
      });
      
      return deferred.promise;
    }
    
    // get the bpmn20xml
    function loadBpmn20Xml() {
      var deferred = $q.defer();
      
      ProcessDefinitionResource.getBpmn20Xml({ id: $scope.processDefinitionId }).$then(function(response) {
        deferred.resolve(response.data);
      });
      
      return deferred.promise;
    }
    
    // get the activity instance tree
    function loadActivityInstances() {
      var deferred = $q.defer();
      
      ProcessInstanceResource.activityInstances({ id: $scope.processInstanceId }).$then(function(response) {
        deferred.resolve(response.data);
      });
      
      return deferred.promise;
    }

    // get the incidents
    function loadIncidents() {
      var deferred = $q.defer();
      
      IncidentResource.query({ id : $scope.processInstanceId }).$then(function(response) {
        deferred.resolve(response.data);
      });
      
      return deferred.promise;
    }
    
    $q.all([ loadProcessDefinition(),
             loadBpmn20Xml(),
             loadActivityInstances(),
             loadIncidents() 
           ])
      .then(function(results) {
        // first result is the process definition
        var processDefinition = results[0];
        $scope.processInstance.processDefinition = processDefinition;
        
        // second result is the bpmn20Xml
        var bpmn20Xml = results[1].bpmn20Xml;
        $scope.processInstance.semantic = Transform.transformBpmn20Xml(bpmn20Xml);

        // contains for each activity id the corresponding bpmnElement
        $scope.processInstance.activityIdToBpmnElementMap = {};
        createActivityIdToBpmnElementMap($scope.processInstance.semantic, $scope.processInstance.activityIdToBpmnElementMap);
        
        // third result is the activity instance tree
        var activityInstances = results[2];
        $scope.processInstance.activityInstanceTree = activityInstances;

        // contains for each activity id the corresponding activity/transition instance
        $scope.processInstance.activityIdToInstancesMap = {};
        // contains for each activity/transition id the corresponding activity/transition instance
        $scope.processInstance.instanceIdToInstanceMap = {};

        decorateActivityInstanceTree($scope.processInstance.activityInstanceTree, $scope.processInstance.activityIdToBpmnElementMap, $scope.processInstance.activityIdToInstancesMap, $scope.processInstance.instanceIdToInstanceMap, $scope.processInstance.semantic, processDefinition);
       
        // contains activity id the current count of activity/transition instances
        $scope.processInstance.activityInstanceStatistics = [];

        // contains the activity ids which are clickable on the process diagram
        $scope.processInstance.clickableElements = [];
        
        $scope.processInstance.activityInstanceIdToActivityIdMap = {};

        for (key in $scope.processInstance.activityIdToInstancesMap) {
          var items = $scope.processInstance.activityIdToInstancesMap[key];
          var activityInstanceStatistics = { id: key, count: items.length};
          $scope.processInstance.activityInstanceStatistics.push(activityInstanceStatistics);
          
          $scope.processInstance.clickableElements.push(key);
        }
              
        // fourth result are the incidents
        var incidents = results[3];
        
        // contains for each activity id the corresponding incidents
        $scope.processInstance.incidentsStatistics = {};

        // get for each activity the incidents such as:
        // {ServiceTask_1: [firstIncident, secondIncident, ...], ServiceTask_2: [...], ...}
        angular.forEach(incidents, function (incident) {
          var activity = $scope.processInstance.incidentsStatistics[incident.activityId];
          if (!activity) {
            activity = [];
            $scope.processInstance.incidentsStatistics[incident.activityId] = activity;
          }
          activity.push(incident);
        });
        
        $scope.processInstance.incidentsOnActivityArray = [];
        for (var key in $scope.processInstance.incidentsStatistics) {
          var tmp = {};
          tmp.id = key;
          tmp.incidents = $scope.processInstance.incidentsStatistics[key];
          $scope.processInstance.incidentsOnActivityArray.push(tmp);
        }
      });
    
    function createActivityIdToBpmnElementMap(semantic, result) {
      angular.forEach(semantic, function(currentSemantic) {
        result[currentSemantic.id] = currentSemantic;
        
        if (currentSemantic.baseElements) {
          createActivityIdToBpmnElementMap(currentSemantic.baseElements, result);
        }
        
      });
    }

    function decorateActivityInstanceTree(activityInstanceTree, activityIdToBpmnElementMap, activityIdToInstancesMap, instanceIdToInstanceMap, semantic, processDefinition) {

      // the root node has to be handled quite different then the child nodes
      var model = null;
      for (var i = 0; i < semantic.length; i++) {
        var currentSemantic = semantic[i];
        if (currentSemantic.id === processDefinition.key && currentSemantic.type === 'process') {
          model = currentSemantic;
        }
      }
      activityIdToBpmnElementMap[activityInstanceTree.activityId] = model;
      activityInstanceTree.name = getActivityName(model);

      // if (activityIdToInstancesMap) {
      //   activityIdToInstancesMap[model.id] = [ activityInstanceTree ];
      // }

      if (instanceIdToInstanceMap) {
        instanceIdToInstanceMap[activityInstanceTree.id] = activityInstanceTree;
      }

      decorateActivityInstanceTreeHelper(activityInstanceTree, activityIdToBpmnElementMap, activityIdToInstancesMap, instanceIdToInstanceMap);
    }

    function decorateActivityInstanceTreeHelper(activityInstanceTree, activityIdToBpmnElementMap, activityIdToInstancesMap, instanceIdToInstanceMap) {
      var children = activityInstanceTree.childActivityInstances;
      if (children) {
        for (var i = 0; i < children.length; i++) {
          var child = children[i];
          decorateActivityInstanceTreeHelper(child, activityIdToBpmnElementMap, activityIdToInstancesMap, instanceIdToInstanceMap)

          var bpmnElement = activityIdToBpmnElementMap[child.activityId];
          child.name = getActivityName(bpmnElement);

          if (activityIdToInstancesMap) {
            var instances = activityIdToInstancesMap[child.activityId];
            if (!instances) {
              instances = [];
              activityIdToInstancesMap[child.activityId] = instances;
            }
            instances.push(child);            
          }
          
          if (instanceIdToInstanceMap) {
            instanceIdToInstanceMap[child.id] = child;
          }

        }
      }

      var transitions = activityInstanceTree.childTransitionInstances;
      if (transitions) {
        for (var i = 0; i < transitions.length; i++) {
          var transition = transitions[i];
          
          var bpmnElement = activityIdToBpmnElementMap[transition.targetActivityId];
          transition.name = getActivityName(bpmnElement);

          if (activityIdToInstancesMap) {
            var instances = activityIdToInstancesMap[transition.targetActivityId];
            if (!instances) {
              instances = [];
              activityIdToInstancesMap[transition.targetActivityId] = instances;
            }
            instances.push(transition);            
          }
          
          if (instanceIdToInstanceMap) {
            instanceIdToInstanceMap[transition.id] = transition;
          }
        };     
      }
    }

    function getActivityName(bpmnElement) {
      var name = bpmnElement.name;
      if (!name) {
        var shortenFilter = $filter('shorten');
        name = bpmnElement.type + ' (' + shortenFilter(bpmnElement.id, 8) + '...)';
      }

      return name;
    }

    $scope.processInstanceVars = { read: [ 'processInstanceId', 'processInstance', 'selection' ] };
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

  module.controller('ProcessInstanceController', [ '$scope', '$routeParams', '$location', '$q', '$filter', 'ProcessDefinitionResource', 'ProcessInstanceResource', 'IncidentResource', 'Views', 'Transform', ProcessInstanceController ]);

  var RouteConfig = function ($routeProvider) {
    $routeProvider.when('/process-definition/:processDefinitionId/process-instance/:processInstanceId', {
      templateUrl: 'pages/process-instance.html',
      controller: 'ProcessInstanceController',
      reloadOnSearch: false
    });
  };

  RouteConfig.$inject = ['$routeProvider'];

  module
    .config(RouteConfig);
});
