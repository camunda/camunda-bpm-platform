ngDefine('cockpit.pages', [ 'angular' ], function(module, angular) {

  var Controller = function($scope, Notifications, ProcessDefinitionResource, ProcessInstanceResource, Views, Transform, processDefinition) {

    $scope.processDefinition = processDefinition;

    $scope.processInstanceTable = Views.getProvider({ component: 'cockpit.processDefinition.instancesTable' });

    $scope.selection = {};
    
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
    
    ProcessInstanceResource.count({ processDefinitionKey : processDefinition.key }).$then(function(response) {
      $scope.processDefinitionTotalCount = response.data;
    });
    
    ProcessInstanceResource.count({ processDefinitionId : processDefinition.id }).$then(function(response) {
      $scope.processDefinitionLatestVersionCount = response.data;
    });
    
    ProcessDefinitionResource.getBpmn20Xml({ id : processDefinition.id}).$then(function(response) {
      $scope.semantic = Transform.transformBpmn20Xml(response.data.bpmn20Xml);
    });
    
    ProcessDefinitionResource.queryActivityStatistics({ id : processDefinition.id, incidents: true }).$then(function(response) {
      $scope.activityStatistics = [];
      $scope.incidents = [];
      
      angular.forEach(response.data, function(currentStatistics) {
        var statistics = { id: currentStatistics.id, count: currentStatistics.instances };
        $scope.activityStatistics.push(statistics);
        
        var incident = { id: currentStatistics.id, incidents: currentStatistics.incidents };
        $scope.incidents.push(incident);
      });
      
    });
    
  };

  Controller.$inject = [ '$scope', 'Notifications', 'ProcessDefinitionResource', 'ProcessInstanceResource', 'Views', 'Transform', 'processDefinition' ];

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
