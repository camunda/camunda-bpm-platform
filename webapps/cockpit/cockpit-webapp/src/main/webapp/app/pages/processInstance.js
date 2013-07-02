ngDefine('cockpit.pages', function(module) {

  function ProcessInstanceController ($scope, $routeParams, $location, $q, ProcessDefinitionResource, ProcessInstanceResource, IncidentResource, Views, ActivityInstance, Transform) {
    
    $scope.processDefinitionId = $routeParams.processDefinitionId;
    $scope.processInstanceId = $routeParams.processInstanceId;

    $scope.selection = {};
    
    $scope.$watch('selection.treeToDiagramMap', function (newValue) {
      if (!newValue) {
        return;
      }

      if (newValue.scrollTo) {
        var bpmnElement = $scope.processInstance.activityIdToBpmnElementMap[newValue.scrollTo.activityId];
        $scope.selection.treeToDiagramMap.scrollTo = null;
        $scope.selection.treeToDiagramMap.scrollToBpmnElement = bpmnElement;
      }
      
      if (newValue.activityInstances) {
        $scope.selection.treeToDiagramMap.bpmnElements = [];
        angular.forEach(newValue.activityInstances, function(activityInstance) {
          var bpmnElement = $scope.processInstance.activityIdToBpmnElementMap[activityInstance.activityId];
          $scope.selection.treeToDiagramMap.bpmnElements.push(bpmnElement);
        });
        return;
      }
      
      if (newValue.bpmnElements) {
        $scope.selection.treeToDiagramMap.activityInstances = [];
        angular.forEach(newValue.bpmnElements, function(bpmnElement) {
          var instanceList = $scope.processInstance.activityIdToNodeMap[bpmnElement.id];
          $scope.selection.treeToDiagramMap.activityInstances = $scope.selection.treeToDiagramMap.activityInstances.concat(instanceList);
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
        $scope.processInstance.activityIdToBpmnElementMap = {};
        createActivityIdToBpmnElementMap($scope.processInstance.semantic, $scope.processInstance.activityIdToBpmnElementMap);
        
        // third result is the activity instance tree
        var activityInstances = results[2];
        $scope.processInstance.activityInstances = activityInstances;
        $scope.processInstance.activityInstanceMap = ActivityInstance.aggregateActivityInstances(activityInstances);
        
        $scope.processInstance.activityInstanceArray = [];
        $scope.processInstance.clickableElements = [];
        
        for (key in $scope.processInstance.activityInstanceMap) {
          var items = $scope.processInstance.activityInstanceMap[key];
          var activityInstance = { id: key, count: items.length};
          $scope.processInstance.activityInstanceArray.push(activityInstance);
          
          $scope.processInstance.clickableElements.push(key);
        }
        
        // create a tree with that results
        $scope.processInstance.activityIdToNodeMap = {};
        $scope.processInstance.activityInstanceTree = ActivityInstance.createActivityInstanceTree(processDefinition, $scope.processInstance.semantic, activityInstances, $scope.processInstance.activityIdToNodeMap);
        
        // fourth result are the incidents
        var incidents = results[3];
        
        $scope.processInstance.incidentsOnActivityMap = {};
        // get for each activity the incidents such as:
        // {ServiceTask_1: [firstIncident, secondIncident, ...], ServiceTask_2: [...], ...}
        angular.forEach(incidents, function (incident) {
          var activity = $scope.processInstance.incidentsOnActivityMap[incident.activityId];
          if (!activity) {
            activity = [];
            $scope.processInstance.incidentsOnActivityMap[incident.activityId] = activity;
          }
          activity.push(incident);
        });
        
        $scope.processInstance.incidentsOnActivityArray = [];
        for (var key in $scope.processInstance.incidentsOnActivityMap) {
          var tmp = {};
          tmp.id = key;
          tmp.incidents = $scope.processInstance.incidentsOnActivityMap[key];
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
    
    $scope.processInstanceVars = { read: [ 'processInstanceId' ] };
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

  module.controller('ProcessInstanceController', [ '$scope', '$routeParams', '$location', '$q', 'ProcessDefinitionResource', 'ProcessInstanceResource', 'IncidentResource', 'Views', 'ActivityInstance', 'Transform', ProcessInstanceController ]);

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
