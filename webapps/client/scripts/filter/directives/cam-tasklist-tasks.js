define([
  'angular',
  'moment',
  'text!./cam-tasklist-tasks.html'
], function(
  angular,
  moment,
  template
) {
  'use strict';

  function indexOfId(items, id) {
    for (var i in items) {
      if (items[i].id === id) { return i; }
    }
    return -1;
  }

  return [function(){

    return {

      restrict: 'EAC',
      scope: {
        tasklistData: '='
      },

      template: template,

      controller: [
        '$scope',
        '$location',
      function(
        $scope,
        $location
      ) {

        $scope.pageNum = 1;
        $scope.pageSize = null;
        $scope.totalItems = 0;
        $scope.now = (new Date()).toJSON();

        $scope.filterProperties = null;

        var tasksData = $scope.tasklistData.newChild($scope);

        var query;

        /**
         * observe the list of tasks
         */
        $scope.state = tasksData.observe('taskList', function (taskList) {
          $scope.totalItems = taskList.count;
          $scope.tasks = taskList._embedded.task;
        });

        /**
         * observe the task list query
         */
        tasksData.observe('taskListQuery', function(taskListQuery) {
          // parse pagination properties from query
          query = angular.copy(taskListQuery);
          $scope.pageSize = query.maxResults;
          // Sachbearbeiter starts counting at '1'
          $scope.pageNum = (query.firstResult / $scope.pageSize) + 1;
        });

        tasksData.observe('taskId', function(taskId) {
          $scope.currentTaskId = taskId.taskId;
        });

        /**
         * Observes the properties of the current filter.
         * Used to retrieve information about variables displayed on a task.
         */
        tasksData.observe(['currentFilter', function(currentFilter) {

          $scope.filterProperties = currentFilter !== null ? currentFilter.properties : null;

        }]);

        $scope.focus = function (task) {
          tasksData.set('taskId', { 'taskId' : task.id });
          $scope.currentTaskId = task.id;
        };

        /**
         * invoked when pagination is changed
         */
        $scope.pageChange = function() {
          // update query
          query.firstResult = $scope.pageSize * ($scope.pageNum - 1);
          tasksData.set('taskListQuery', query);
        };

        $scope.getHrefUrl = function (task) {
          var href = '#/?task=' + task.id;
          var detailsTab = $location.search().detailsTab;
          if (detailsTab) {
            href = href + '&detailsTab=' + detailsTab;
          }

          return href;
        };

      }]
    };
  }];
});
