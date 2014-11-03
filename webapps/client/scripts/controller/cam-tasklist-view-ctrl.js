define([
], function(
) {
  'use strict';

  function getRefreshProvider(tasklistData) {
    return {
      refreshTaskList : function () {
        tasklistData.changed('taskList');
      }
    };
  }

  return [
    '$scope',
    '$q',
    '$location',
    '$interval',
    'search',
    'dataDepend',
    'camAPI',
  function(
    $scope,
    $q,
    $location,
    $interval,
    search,
    dataDepend,
    camAPI
  ) {

    function getPropertyFromLocation(property) {
      var search = $location.search() || {};
      return search[property] || null;
    }

    $scope.$on('$destroy', function () {
      $scope.tasklistApp.refreshProvider = null;
    });

    // init data depend for task list data
    var tasklistData = $scope.tasklistData = dataDepend.create($scope);

    if ($scope.tasklistApp) {
      $scope.tasklistApp.refreshProvider = getRefreshProvider(tasklistData);
    }

    // get current task id from location
    var taskId = getPropertyFromLocation('task');
    var detailsTab = getPropertyFromLocation('detailsTab');

    var Filter = camAPI.resource('filter');
    var Task = camAPI.resource('task');

    /**
     * Provides the list of filters
     */
    tasklistData.provide('filters', [ function() {
      var deferred = $q.defer();
      Filter.list({
        itemCount: false,
        resoureType: 'Task'
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

    var currentFilter;
    tasklistData.provide('currentFilter', ['filters', function(filters) {

      var focused,
          filterId = getPropertyFromLocation('filter');

      for (var i = 0, filter; !!(filter = filters[i]); i++) {

          if (filterId === filter.id) {
            focused = filter;
            break;
          }
          // auto focus first filter
          if(!focused || filter.properties.priority < focused.properties.priority) {
            focused = filter;
          }
      }
      if(currentFilter) {
        search.updateSilently({
          page: "1"
        });
      }
      if(focused.id !== filterId) {
        search.updateSilently({
          filter: focused.id
        });
      }
      return angular.copy(focused);
    }]);

    tasklistData.observe('currentFilter', function(_currentFilter) {
      currentFilter = _currentFilter;
    });
    var operatorTable = {
      "<" : "lt",
      ">" : "gt",
      "=" : "eq",
      "!=": "neq",
      ">=": "gteq",
      "<=": "lteq",
      "like":"like",
      "BEFORE":"lteq",
      "AFTER":"gteq"
    };
    var typeTable = {
      "Process Variable" : "processVariables",
      "Task Variable" : "taskVariables",
      "Case Variable" : "caseInstanceVariables"
    };
    tasklistData.provide('taskListQuery', ['currentFilter', function(currentFilter) {
      var searches = JSON.parse(getPropertyFromLocation("query"));
      var query = {};
      query.processVariables = [];
      query.taskVariables = [];
      query.caseInstanceVariables = [];
      angular.forEach(searches, function(search) {
         query[typeTable[search.type]].push({
           name: search.name,
           operator: operatorTable[search.operator],
           value: search.value
         });
      });
      return {
        id : currentFilter.id,
        firstResult : ($location.search().page - 1 || 0) * 15,
        maxResults : 15,
        sortBy : $location.search().sortBy || 'priority',
        sortOrder: $location.search().sortOrder || 'asc',
        active: true,
        processVariables : query.processVariables, //$scope.tasklistApp.searchProvider.get("processVariables"),
        taskVariables : query.taskVariables,
        caseInstanceVariables : query.caseInstanceVariables
      };
    }]);

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


    // automatically refresh the taskList every 10 seconds so that changes (such as claims) are represented in realtime
    var intervalPromise;
    tasklistData.observe("currentFilter", function(currentFilter) {
      // stop current refresh
      if(intervalPromise) {
        $interval.cancel(intervalPromise);
      }
      if(currentFilter && currentFilter.properties.refresh) {
        intervalPromise = $interval(function(){
          if($scope.tasklistApp && $scope.tasklistApp.refreshProvider) {
            $scope.tasklistApp.refreshProvider.refreshTaskList();
          } else {
            $interval.cancel(intervalPromise);
          }
        }, 10000);
      }
    });

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
    $scope.$on('$routeChanged', function(prev, current) {
      var oldTaskId = taskId;
      var oldDetailsTab = detailsTab;

      taskId = getPropertyFromLocation('task');
      detailsTab = getPropertyFromLocation('detailsTab');

      if (oldTaskId !== taskId || oldDetailsTab === detailsTab) {
        tasklistData.set('taskId', { 'taskId' : taskId });
      }

      currentFilter = null;
      tasklistData.changed('currentFilter');

    });
  }];
});
