ngDefine('cockpit.pages', function(module) {

  var Controller = function($scope, $routeParams, $location, $q, ProcessDefinitionResource, ProcessInstanceResource, IncidentResource, Views, ActivityInstance, Transform) {
    
    $scope.processDefinitionId = $routeParams.processDefinitionId;
    $scope.processInstanceId = $routeParams.processInstanceId;

    $scope.selection = {};
    
    $scope.$watch(function() { return $scope.selection.selectedBpmnElements; }, function(newValue, oldValue) {
      if (newValue && newValue.length == 0) {
        return;
      }
      if (newValue && newValue != oldValue) {
        
        $scope.selection.selectedActivityIdsInProcessDiagram = [];
        $scope.selection.selectedActivityInstances = [];
        $scope.selection.selectedActivityIdsInTree = [];
        
        angular.forEach(newValue, function(selectedBpmnElement) {
          $scope.selection.selectedActivityIdsInTree.push(selectedBpmnElement.id);
        });
      }
    });
    
    $scope.$watch(function() { return $scope.selection.selectedActivityInstances; }, function(newValue, oldValue) {
      if (newValue && newValue.length == 0) {
        return;
      }
      if (newValue && newValue != oldValue) {
        
        $scope.selection.selectedActivityIdsInProcessDiagram = [];
        $scope.selection.selectedActivityIdsInTree = [];
        $scope.selection.selectedBpmnElements = [];
        
        angular.forEach(newValue, function(selectedActivityInstance) {
          $scope.selection.selectedActivityIdsInProcessDiagram.push(selectedActivityInstance.activityId);
        });
      }
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
        
        // third result is the activity instance tree
        var activityInstances = results[2];
        $scope.processInstance.activityInstances = activityInstances;
        $scope.processInstance.activityInstanceMap = ActivityInstance.aggregateActivityInstances(activityInstances);
        
        $scope.processInstance.activityInstanceArray = [];
        
        for (key in $scope.processInstance.activityInstanceMap) {
          var items = $scope.processInstance.activityInstanceMap[key];
          var activityInstance = { id: key, count: items.length};
          $scope.processInstance.activityInstanceArray.push(activityInstance);
        }
        
        // create a tree with that results
        $scope.processInstance.activityInstanceTree = ActivityInstance.createActivityInstanceTree(processDefinition, $scope.processInstance.semantic, activityInstances);
        
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

  Controller.$inject = [ '$scope', '$routeParams', '$location', '$q', 'ProcessDefinitionResource', 'ProcessInstanceResource', 'IncidentResource', 'Views', 'ActivityInstance', 'Transform' ];

  var RouteConfig = function ($routeProvider) {
    $routeProvider.when('/process-definition/:processDefinitionId/process-instance/:processInstanceId', {
      templateUrl: 'pages/process-instance.html',
      controller: Controller,
      reloadOnSearch: false
    });
  };

  RouteConfig.$inject = ['$routeProvider'];

  module
    .config(RouteConfig);
});
