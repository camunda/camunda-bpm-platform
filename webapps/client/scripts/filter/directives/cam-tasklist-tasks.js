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
        'search',
      function(
        $scope,
        $location,
        search
      ) {

        function updateSilently(params) {
          search.updateSilently(params);
        }

        $scope.pageNum = 1;
        $scope.pageSize = null;
        $scope.totalItems = 0;
        $scope.now = (new Date()).toJSON();

        $scope.filterProperties = null;

        var tasksData = $scope.tasklistData.newChild($scope);

        $scope.query = {};

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
          if (taskListQuery) {
            // parse pagination properties from query
            $scope.query = angular.copy(taskListQuery);
            $scope.pageSize = $scope.query.maxResults;
            // Sachbearbeiter starts counting at '1'
            $scope.pageNum = ($scope.query.firstResult / $scope.pageSize) + 1;
          }
        });

        tasksData.observe('taskId', function(taskId) {
          $scope.currentTaskId = taskId.taskId;
        });

        /**
         * Observes the properties of the current filter.
         * Used to retrieve information about variables displayed on a task.
         */
        tasksData.observe(['currentFilter', function(currentFilter) {
          if (currentFilter) {
            $scope.filterProperties = currentFilter !== null ? currentFilter.properties : null;
          }
        }]);

        $scope.focus = function ($event, task) {
          if ($event) {
            $event.preventDefault();
          }

          var taskId = task.id;
          tasksData.set('taskId', { 'taskId' : taskId });
          $scope.currentTaskId = taskId;

          var searchParams = $location.search() || {};
          searchParams.task = taskId;
          updateSilently(searchParams);
        };

        /**
         * invoked when pagination is changed
         */
        $scope.pageChange = function() {
          // update query
          updateSilently({
            page:  $scope.pageNum
          });
          tasksData.changed('taskListQuery');
        };

         $scope.resetPage = function() {
           updateSilently({
             page: 1
           });
           tasksData.changed('taskListQuery');
         };

      }]
    };
  }];
});
