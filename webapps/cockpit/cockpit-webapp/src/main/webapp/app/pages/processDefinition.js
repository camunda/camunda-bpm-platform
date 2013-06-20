ngDefine('cockpit.pages', [ 'angular' ], function(module, angular) {

  var Controller = function($scope, Notifications, ProcessInstanceResource, Views, processDefinition) {

    $scope.processDefinition = processDefinition;

    $scope.processInstanceTable = Views.getProvider({ component: 'cockpit.processDefinition.instancesTable' });

    ProcessInstanceResource.count({ processDefinitionKey : processDefinition.key }).$then(function(response) {
      $scope.processDefinitionTotalCount = response.data;
    });

    ProcessInstanceResource.count({ processDefinitionId : processDefinition.id }).$then(function(response) {
      $scope.processDefinitionLatestVersionCount = response.data;
    });
  };

  Controller.$inject = [ '$scope', 'Notifications', 'ProcessInstanceResource', 'Views', 'processDefinition' ];

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
                return ProcessDefinitionResource.get({ id : id })
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
