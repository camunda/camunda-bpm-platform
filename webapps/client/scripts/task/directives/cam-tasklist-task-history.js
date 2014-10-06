define([
  'angular',
  'jquery',
  'moment',
  'camunda-bpm-sdk',
  'text!./cam-tasklist-task-history.html'
], function(
  angular,
  jquery,
  moment,
  camSDK,
  template
) {
  'use strict';
  
  var findOrCreateDay = function(days, timestamp) {
    var day = jquery.grep(days, function(elem) {
      return moment(elem.date/1000, 'X').format('YYYY-MM-DD') === moment(timestamp/1000, 'X').format('YYYY-MM-DD');
    });
    if(day.length > 0) {
      return day[0];
    } else {
      day = {
        date: timestamp,
        events: []
      };
      days.push(day);
      return day;
    }
  };
  
  var findOrCreateEvent = function(events, event, timestamp) {
    var targetEvent = jquery.grep(events, function(elem) {
      return elem.operationId === event.operationId;
    });
    if(targetEvent.length > 0) {
      return targetEvent[0];
    } else {
      targetEvent = {
        time: timestamp,
        type: event.operationType,
        operationId: event.operationId,
        userId: event.userId, 
        subEvents: []
      };
      events.push(targetEvent);
      return targetEvent;
    }
  };
  
  return ['camAPI',
  function(camAPI) {
    var History = camAPI.resource('history');
    var Task = camAPI.resource('task');
    return {
      scope: {
        task : '='
      },
      link: function($scope) {
        $scope.history = [];
        $scope.days = [];

        var loadHistory = function(taskId) {
          camSDK.utils.series({
            historyData: function(cb) { History.userOperation({taskId : taskId}, cb); },
            commentData: function(cb) { Task.comments(taskId, cb); }
          }, function(err, data) {
            $scope.history = data.historyData;
            var days = [];
            angular.forEach($scope.history, function(event) {
              var mom = moment(event.timestamp, 'YYYY-MM-DDTHH:mm:ss');
              var timestamp = mom.format('X')*1000;

              // create object for each day, containing the events for this day
              var day = findOrCreateDay(days, timestamp);

              // create event object for each operationId
              var parentEvent = findOrCreateEvent(day.events, event, timestamp);

              parentEvent.subEvents.push(event);
            });
            $scope.days = days;
          });
        };
        loadHistory($scope.task.id);
        $scope.$on('tasklist.task.current', function(evt) {
          loadHistory(evt.targetScope.currentTask.id);
        });
        $scope.$on('tasklist.task.update', function(evt) {
          loadHistory(evt.targetScope.currentTask.id);
        });
      },
      template: template
    };
  }];
});
