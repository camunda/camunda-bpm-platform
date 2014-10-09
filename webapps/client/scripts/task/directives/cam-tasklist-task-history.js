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
      return moment(elem.date).format('YYYY-MM-DD') === moment(timestamp).format('YYYY-MM-DD');
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

  var findOrCreateParentEvent = function(events, event) {
    var parentEvent = jquery.grep(events, function(elem) {
      return elem.operationId === event.operationId;
    });
    if(parentEvent.length > 0) {
      return parentEvent[0];
    } else {
      parentEvent = {
        time: event.timestamp,
        type: event.operationType,
        operationId: event.operationId,
        userId: event.userId,
        subEvents: []
      };
      events.push(parentEvent);
      return parentEvent;
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

        function isTimestampProperty(propertyName) {
          return ['dueDate', 'followUpDate'].indexOf(propertyName) !== -1;
        }

        var loadHistory = function(taskId) {
          camSDK.utils.series({
            historyData: function(cb) { History.userOperation({taskId : taskId}, cb); },
            commentData: function(cb) { Task.comments(taskId, cb); }
          }, function(err, data) {
            if(err) {
              $scope.error = err;
            } else {
              $scope.error = null;

              $scope.history = data.historyData;
              $scope.comments = data.commentData;

              var days = [];
              angular.forEach($scope.history, function(event) {
                // create object for each day, containing the events for this day
                var day = findOrCreateDay(days, event.timestamp);

                // create event object for each operationId
                var parentEvent = findOrCreateParentEvent(day.events, event);

                // preprocess the dates to avoid function calls from the template
                if (isTimestampProperty(event.property)) {
                  event.propertyIsDate = true;
                  event.newValue = event.newValue ? parseInt(event.newValue, 10) : null;
                  event.orgValue = event.orgValue ? parseInt(event.orgValue, 10) : null;
                }

                parentEvent.subEvents.push(event);
              });

              angular.forEach($scope.comments, function(comment) {
                var day = findOrCreateDay(days, comment.time);
                comment.type = 'Comment';
                day.events.push(comment);
              });

              $scope.days = days;
            }
          });
        };
        $scope.$on('tasklist.task.tab', function(evt, tabName) {
          if(tabName === 'history') {
            loadHistory($scope.task.id);
          }
        });

        function loadHistoryAfterEvent(evt) {
          loadHistory(evt.targetScope.currentTask.id);
        }
        $scope.$on('tasklist.task.current', loadHistoryAfterEvent);
        $scope.$on('tasklist.task.update',  loadHistoryAfterEvent);
        $scope.$on('tasklist.comment.new',  loadHistoryAfterEvent);
      },
      template: template
    };
  }];
});
