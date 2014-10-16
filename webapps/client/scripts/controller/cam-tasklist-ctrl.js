define([
], function(
) {
  'use strict';

  function getTaskIdFromLocation($location) {
    return $location.search().task;
  }

  return [
    '$scope',
    '$q',
    '$location',
    'dataDepend',
    'camAPI',
  function(
    $scope,
    $q,
    $location,
    dataDepend,
    camAPI
  ) {

    // get current task id from location
    var taskId = getTaskIdFromLocation($location);

    // init data depend for task list data
    var tasklistData = $scope.tasklistData = dataDepend.create($scope);

    var Filter = camAPI.resource('filter');
    var Task = camAPI.resource('task');

    /**
     * Provides the list of filters
     */
    tasklistData.provide('filters', [ function() {
      var deferred = $q.defer();

      Filter.list({
        itemCount: false
      }, function(err, res) {
        if(!!err) {
          deferred.reject(err);
        }
        else {
          deferred.resolve(res);
        }
      });

      return deferred.promise;
    }]);

    /**
     * Provide the initial task list query
     */
    tasklistData.provide('taskListQuery', {
      id : null, // initially the id of the filter is null
      firstResult : 0,
      maxResults : 15,
      sortBy : 'priority',
      sortOrder: 'asc'
    });

    /**
     * Provide the list of tasks
     */
    tasklistData.provide('taskList', [ 'taskListQuery', function(taskListQuery) {
      var deferred = $q.defer();

      if(taskListQuery.id === null) {
        // no filter selected
        deferred.resolve({
          count: 0,
          _embedded : {}
        });
      }
      else {
        // filter selected
        Filter.getTasks(angular.copy(taskListQuery), function(err, res) {
          if(err) {
            deferred.reject(err);
          }
          else {
            deferred.resolve(res);
          }
        });
      }

      return deferred.promise;
    }]);

    tasklistData.provide('currentFilter', ['taskListQuery', 'filters', function(taskListQuery, filters) {
      if(taskListQuery.id === null) {
        return null;
      }
      else {
        for (var f in filters) {
          if (filters[f].id === taskListQuery.id) {
            return filters[f];
          }
        }
        return null;
      }
    }]);

   /**
     * Provide current task id
     */
    tasklistData.provide('taskId', { 'taskId' : taskId });


    /**
     * Provide the current task or the value 'null' in case no task is selected
     */
    tasklistData.provide('task', ['taskId', function(task) {

      var deferred = $q.defer();

      var taskId = task.taskId;

      if(typeof taskId !== 'string') {
        deferred.resolve(null);
      }
      else {
        Task.get(taskId, function(err, res) {
          if(err) {
            deferred.reject(err);
          }
          else {
            deferred.resolve(res);
          }

        });
      }

      return deferred.promise;
    }]);

    /**
     * Update task if location changes
     */
    $scope.$on('$locationChangeSuccess', function(prev, current) {
      taskId = getTaskIdFromLocation($location);
      tasklistData.set('taskId', { 'taskId' : taskId });
    });

  }];

});
