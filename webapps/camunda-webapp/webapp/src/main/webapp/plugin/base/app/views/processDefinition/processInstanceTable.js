ngDefine('cockpit.plugin.base.views', function(module) {

  var Controller = function ($scope, $routeParams, $location, PluginProcessInstanceResource, ProcessInstanceResource) {

    var processDefinitionId = $scope.processDefinitionId = $routeParams.processDefinitionId;

    var pages = $scope.pages = { size: 50, total: 0 };

    $scope.$watch(function() { return $location.search().page; }, function(newValue) {
      pages.current = parseInt(newValue) || 1;
    });

    $scope.$watch('pages.current', function(newValue) {
      var currentPage = newValue || 1;
      var search = $location.search().page;

      if (search || currentPage !== 1) {
        $location.search('page', currentPage);
      }

      updateView(currentPage);
    });

    function updateView(page) {
      var count = pages.size;
      var firstResult = (page - 1) * count;

      PluginProcessInstanceResource.query({
        processDefinitionId: processDefinitionId,
        firstResult: firstResult,
        maxResults: count
      }).$then(function(data) {
        $scope.processInstances = data.resource;
      });

      ProcessInstanceResource.count({
        processDefinitionId : processDefinitionId
      }).$then(function(data) {
        pages.total = Math.ceil(data.data.count / pages.size);
      });
    };

  };

  Controller.$inject = [ '$scope', '$routeParams', '$location', 'PluginProcessInstanceResource', 'ProcessInstanceResource' ];

  var Configuration = function PluginConfiguration(ViewsProvider) {

    ViewsProvider.registerDefaultView('cockpit.processDefinition.instancesTable', {
      id: 'process-instances-table',
      label: 'Process Instances',
      url: 'plugin://base/static/app/views/processDefinition/process-instance-table.html',
      controller: Controller
    });
  };

  Configuration.$inject = ['ViewsProvider'];

  module.config(Configuration);
});
