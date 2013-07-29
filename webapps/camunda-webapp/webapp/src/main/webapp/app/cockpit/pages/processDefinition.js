ngDefine('cockpit.pages', [ 'angular' ], function(module, angular) {

  var Controller = function($scope, $rootScope, $location, Notifications, ProcessDefinitionResource, ProcessInstanceResource, Views, Transform, processDefinition) {

    $scope.processDefinition = processDefinition;
    $scope.currentVersion = processDefinition.version;

    var parentProcessDefinitionId = $location.search().parentProcessDefinitionId || null;

    $scope.processDefinitions;

    $scope.selection = {};


    $scope.$on('$routeChangeStart', function () {
      $rootScope.clearBreadcrumbs();
    });

    // add process definition breadcrumb
    if (!parentProcessDefinitionId) {
      $rootScope.addBreadcrumb({'type': 'processDefinition', 'processDefinition': processDefinition});  
    } else {
      ProcessDefinitionResource.get({ id : parentProcessDefinitionId }).$then(function(response) {
        $rootScope.addBreadcrumb({'type': 'processDefinition', 'processDefinition': response.data});  
        $rootScope.addBreadcrumb({'type': 'processDefinition', 'processDefinition': processDefinition});  
      });
    }
  
    $scope.processDefinitionVars = { read: [ 'processDefinition', 'selection' ] };
    $scope.processDefinitionViews = Views.getProviders({ component: 'cockpit.processDefinition.view' });

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
        var provider = Views.getProvider({ component: 'cockpit.processDefinition.view', id: selectedTabId });
        if (provider && tabs.indexOf(provider) != -1) {
          $scope.selectedView = provider;
          return;
        }
      }

      $location.search('detailsTab', null);
      $scope.selectedView = tabs[0];
    }

    setDefaultTab($scope.processDefinitionViews);

    ProcessInstanceResource.count({ processDefinitionKey : processDefinition.key }).$then(function(response) {
      $scope.processDefinitionTotalCount = response.data;
    });

    ProcessDefinitionResource.query({ 'key' : processDefinition.key, 'sortBy': 'version', 'sortOrder': 'asc' }).$then(function(response) {
      $scope.processDefinitions = response.resource;
    });

    ProcessInstanceResource.count({ processDefinitionId : processDefinition.id }).$then(function(response) {
      $scope.processDefinitionCurrentVersionCount = response.data;
    });
    
    ProcessDefinitionResource.getBpmn20Xml({ id : processDefinition.id}).$then(function(response) {
      $scope.semantic = Transform.transformBpmn20Xml(response.data.bpmn20Xml);
      $scope.processDefinition.bpmnElements = getBpmnElements(processDefinition, $scope.semantic);
    });

    function getBpmnElements (processDefinition, semantic) {
      var result = {};

      var key = processDefinition.key;

      var diagram = null;

      for (var i = 0; i < semantic.length; i++) {
        var currentDiagram = semantic[i];
        if (currentDiagram.type === 'process') {
          
          if (currentDiagram.id === key) {
            diagram = currentDiagram;
            break;
          }
        }
      }

      getBpmnElementsHelper(diagram, result);

      return result;
    }

    function getBpmnElementsHelper(element, result) {
      result[element.id] = element;

      if (element.baseElements) {
        angular.forEach(element.baseElements, function(baseElement) {
          getBpmnElementsHelper(baseElement, result);
        });
      }

    }
    
    ProcessDefinitionResource.queryActivityStatistics({ id : processDefinition.id, incidents: true }).$then(function(response) {
      $scope.activityStatistics = [];
      $scope.incidents = [];
      $scope.clickableElements = [];

      angular.forEach(response.data, function(currentStatistics) {
        var statistics = { id: currentStatistics.id, count: currentStatistics.instances };
        $scope.activityStatistics.push(statistics);
        $scope.clickableElements.push(currentStatistics.id);
        
        var incident = { id: currentStatistics.id, incidents: currentStatistics.incidents };
        $scope.incidents.push(incident);
      });
      
    });
    
  };

  Controller.$inject = [ '$scope', '$rootScope', '$location', 'Notifications', 'ProcessDefinitionResource', 'ProcessInstanceResource', 'Views', 'Transform', 'processDefinition' ];

  var RouteConfig = [ '$routeProvider', function($routeProvider) {
    $routeProvider.when('/process-definition/:processDefinitionId', {
      templateUrl: 'pages/process-definition.html',
      controller: Controller,
      resolve: {
        processDefinition: ['ResourceResolver', 'ProcessDefinitionResource',
          function(ResourceResolver, ProcessDefinitionResource) {
            return ResourceResolver.getByRouteParam('processDefinitionId', {
              name: 'process definition',
              resolve: function(id) {
                return ProcessDefinitionResource.get({ id : id });
              }
            });
          }]
      },
      reloadOnSearch: false
    });
  }];

  module
    .config(RouteConfig);
});
