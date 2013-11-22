ngDefine('cockpit.plugin.jobDefinition.views', function(module) {

  var Controller = [ '$scope', 'search', 'JobDefinitionResource',
      function ($scope, search, JobDefinitionResource) {

    var processData = $scope.processData.newChild($scope);

    var processDefinition = $scope.processDefinition;
    
    var DEFAULT_PAGES = { size: 50, total: 0, current: 1 };

    var pages = $scope.pages = angular.copy(DEFAULT_PAGES);

    var filter = null;

    $scope.$watch('pages.current', function(newValue, oldValue) {
      if (newValue == oldValue) {
        return;
      }

      search('page', !newValue || newValue == 1 ? null : newValue);
    });

    processData.observe([ 'filter', 'bpmnElements' ], function(newFilter, bpmnElements) {
      pages.current = newFilter.page || 1;

      updateView(newFilter, bpmnElements);
    });

    function updateView(newFilter, bpmnElements) {

      filter = angular.copy(newFilter);

      delete filter.page;

      var page = pages.current,
          count = pages.size,
          firstResult = (page - 1) * count;

      var defaultParams = {
        processDefinitionId: processDefinition.id
      };

      var pagingParams = {
        firstResult: firstResult,
        maxResults: count
      };

      var countParams = angular.extend({}, filter, defaultParams);

      // fix missmatch -> activityIds -> activityIdIn
      countParams.activityIdIn = countParams.activityIds;
      delete countParams.activityIds;

      var params = angular.extend({}, countParams, pagingParams);

      $scope.jobDefinitions = null;

      JobDefinitionResource.query(pagingParams, params).$then(function(data) {
        angular.forEach(data.resource, function (jobDefinition) {
          var activityId = jobDefinition.activityId;
          var bpmnElement = bpmnElements[activityId];
          jobDefinition.activityName = bpmnElement.name || bpmnElement.id;

          if (!jobDefinition.suspended) {
            jobDefinition.state = "Active";
          } else {
            jobDefinition.state = "Suspended";
          }
        });

        $scope.jobDefinitions = data.resource;
      });

      JobDefinitionResource.count(countParams).$then(function(data) {
        pages.total = Math.ceil(data.data.count / pages.size);
      });
    };
  }];

  var Configuration = function PluginConfiguration(ViewsProvider) {

    ViewsProvider.registerDefaultView('cockpit.processDefinition.view', {
      id: 'job-definition-table',
      label: 'Job Definitions',
      url: 'plugin://jobDefinition/static/app/views/processDefinition/job-definition-table.html',
      controller: Controller,
      priority: 2
    }); 
  };

  Configuration.$inject = ['ViewsProvider'];

  module.config(Configuration);
});
