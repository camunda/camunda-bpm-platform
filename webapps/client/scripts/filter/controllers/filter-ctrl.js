define([
], function(
) {
  'use strict';

return [
  '$scope',
  '$q',
  'dataDepend',
  'camAPI',
function(
  $scope,
  $q,
  dataDepend,
  camAPI
) {
  // init data depend for filter data
  var filterData = $scope.filterData = dataDepend.create($scope);

  /**
   * Provides the list of filters
   */
  filterData.provide('filters', [ function() {
    var deferred = $q.defer();

    camAPI.resource('filter').list({
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
  filterData.provide('taskListQuery', {
    id : null, // initially the id of the filter is null
    firstResult : 0,
    maxResults : 15,
    sortBy : 'priority',
    sortOrder: 'asc'
  });

  /**
   * Provide the list of tasks
   */
  filterData.provide('taskList', [ 'taskListQuery', function(taskListQuery) {
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
      camAPI.resource('filter').getTasks(angular.copy(taskListQuery), function(err, res) {
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

}];


});
