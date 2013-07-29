ngDefine('cockpit.plugin.base.views', function(module) {

  var Controller = function ($scope, $location, PluginProcessInstanceResource) {

    // input processDefinition, selection

    var processDefinitionId = $scope.processDefinition.id;
    var parentProcessDefinitionId = $location.search().parentProcessDefinitionId || null;
    var activityIds = null;
    var alreadyUpdated = false;

    var pages = $scope.pages = { size: 50, total: 0 };

    $scope.$watch(function() { return $location.search().page; }, function(newValue) {
      pages.current = parseInt(newValue) || 1;
    });

    $scope.$watch('pages.current', function(newValue) {
      var currentPage = newValue || 1;
      var search = $location.search().page;

      if (search || currentPage !== 1) {
        $location.search('page', currentPage);
        updateView(currentPage);
      }
      
    });

    $scope.$watch(function() {return $location.search().bpmnElements; }, function (newValue) {
      if (!newValue) {
        activityIds = [];
      } else if (angular.isString(newValue)) {
        activityIds = newValue.split(',');
      } else if (angular.isArray(newValue)) {
        activityIds = newValue;
      }

      $location.search('page', null);
      updateView(1);      
    });

    function updateView(page) {
      var count = pages.size;
      var firstResult = (page - 1) * count;

      PluginProcessInstanceResource.query({
        firstResult: firstResult,
        maxResults: count
      }, {
        processDefinitionId: processDefinitionId,
        parentProcessDefinitionId: parentProcessDefinitionId,
        activityIdIn: activityIds,
        sortBy: 'startTime',
        sortOrder: 'asc'
      }).$then(function(data) {
        $scope.processInstances = data.resource;
      });

      PluginProcessInstanceResource.count({
        processDefinitionId: processDefinitionId,
        parentProcessDefinitionId: parentProcessDefinitionId,
        activityIdIn: activityIds
      }).$then(function(data) {
        pages.total = Math.ceil(data.data.count / pages.size);
      });
    };

  };

  Controller.$inject = [ '$scope', '$location', 'PluginProcessInstanceResource' ];

  var Configuration = function PluginConfiguration(ViewsProvider) {

    ViewsProvider.registerDefaultView('cockpit.processDefinition.view', {
      id: 'process-instances-table',
      label: 'Process Instances',
      url: 'plugin://base/static/app/views/processDefinition/process-instance-table.html',
      controller: Controller,
      priority: 10
    });
  };

  Configuration.$inject = ['ViewsProvider'];

  module.config(Configuration);
});
