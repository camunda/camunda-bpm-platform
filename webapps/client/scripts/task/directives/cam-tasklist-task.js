define([
  'text!./cam-tasklist-task.html'
], function(template) {
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

  function taskIdFromLocation($location) {
    return $location.search().task;
  }

  return [ function() {

    return {
      restrict: 'EAC',
      scope: {
        filterData: '='
      },

      template: template,

      controller : [
        '$scope',
        '$location',
        '$q',
        'dataDepend',
        'camAPI',
      function(
        $scope,
        $location,
        $q,
        dataDepend,
        camAPI
      ) {

        var taskData = $scope.taskData = dataDepend.create($scope);

        // read initial taskId from location
        var taskId = taskIdFromLocation($location);

        /**
         * Provide the current task or the value 'null' in case no task is selected
         */
        taskData.provide('task', function() {

          var deferred = $q.defer();

          if(typeof taskId !== 'string') {
            deferred.resolve(null);
          }
          else {
            camAPI.resource('task')
              .get(taskId, function(err, res) {

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
        });

        /**
         * expose current task as scope variable
         */
        taskData.observe('task', function(task) {
          $scope.task = task;
        });

        /**
         * Update task if location changes
         */
        $scope.$on('$locationChangeSuccess', function(prev, current) {
          taskId = taskIdFromLocation($location);
          taskData.changed('task');
        });

      }]
    };
  }];
});

