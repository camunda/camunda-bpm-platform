ngDefine('cockpit.plugin.base.views', function(module) {

   function UserTaskController ($scope, search, TaskResource) {

    // input: processInstance, processData

    var userTaskData = $scope.processData.newChild($scope),
        processInstance = $scope.processInstance;

    var DEFAULT_PAGES = { size: 50, total: 0, current: 1 };

    var pages = $scope.pages = angular.copy(DEFAULT_PAGES);

    var filter = null;

    $scope.$watch('pages.current', function(newValue, oldValue) {
      if (newValue == oldValue) {
        return;
      }

      search('page', !newValue || newValue == 1 ? null : newValue);
    });     

    userTaskData.observe([ 'filter', 'executionIdToInstanceMap' ], function (newFilter, executionIdToInstanceMap) {
      pages.current = newFilter.page || 1;

      updateView(newFilter, executionIdToInstanceMap);
    });

    function updateView (newFilter, executionIdToInstanceMap) {
      filter = angular.copy(newFilter);

      delete filter.page;
      delete filter.activityIds;
      delete filter.scrollToBpmnElement;

      var page = pages.current,
          count = pages.size,
          firstResult = (page - 1) * count;

      var defaultParams = {
        processInstanceId: processInstance.id,
        processDefinitionId: processInstance.definitionId
      };

      var pagingParams = {
        firstResult: firstResult,
        maxResults: count
      };

      var params = angular.extend({}, filter, defaultParams);

      // fix missmatch -> activityInstanceIds -> activityInstanceIdIn
      params.activityInstanceIdIn = params.activityInstanceIds;
      delete params.activityInstanceIds;

      $scope.userTasks = null;

      TaskResource.count(params).$then(function (response) {      
        pages.total = Math.ceil(response.data.count / pages.size);
      });

      TaskResource.query(pagingParams, params).$then(function (response) {
        for (var i = 0, task; !!(task = response.data[i]); i++) {
          var instance = executionIdToInstanceMap[task.executionId];
          task.instance = instance;
        }

        $scope.userTasks = response.data;
      });

    }

    $scope.getHref = function (userTask) {
      return '#/process-definition/' + processInstance.definitionId + '/process-instance/' + processInstance.id + '?activityInstanceIds=' + userTask.instance.id;
    };

  };

  module.controller('UserTaskController', [ '$scope', 'search', 'TaskResource', UserTaskController ]);

  var Configuration = function PluginConfiguration(ViewsProvider) {

    ViewsProvider.registerDefaultView('cockpit.processInstance.instanceDetails', {
      id: 'user-tasks-tab',
      label: 'User Tasks',
      url: 'plugin://base/static/app/views/processInstance/user-tasks-table.html',
      controller: 'UserTaskController',
      priority: 5
    });
  };

  Configuration.$inject = ['ViewsProvider'];

  module.config(Configuration);
});
