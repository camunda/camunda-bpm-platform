define([
], function(
) {
  'use strict';

  function fixReadDateTimezone(dateStr) {
    if (!dateStr) { return dateStr; }
    var d = new Date(dateStr);
    return (new Date(d.getTime() + (d.getTimezoneOffset() * 60 * 1000))).toJSON();
  }

  function fixDates(task) {
    task.due = fixReadDateTimezone(task.due);
    task.followUp = fixReadDateTimezone(task.followUp);
  }

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

    tasklistData.provide('currentFilterId', ['taskListQuery', function(taskListQuery) {
      return taskListQuery.id;
    }]);

    tasklistData.provide('currentFilter', ['currentFilterId', 'filters', function(currentFilterId, filters) {
      if(currentFilterId === null) {
        return null;
      }
      else {
        for (var f in filters) {
          if (filters[f].id === currentFilterId) {
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
            // adjust dates to current timezone
            fixDates(res);
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
