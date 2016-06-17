'use strict';
var fs = require('fs');

var template = fs.readFileSync(__dirname + '/cam-tasklist-task-detail-history-plugin.html', 'utf8');

var jquery = require('jquery');
var moment = require('camunda-commons-ui/vendor/moment');

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

function isTimestampProperty(propertyName) {
  return ['dueDate', 'followUpDate'].indexOf(propertyName) !== -1;
}

var Controller = [
  '$scope',
  'camAPI',
  '$q',
  function(
    $scope,
    camAPI,
    $q
  ) {

    var History = camAPI.resource('history');
    var Task = camAPI.resource('task');

    var historyData = $scope.taskData.newChild($scope);

    historyData.provide('history', ['task', function(task) {
      var deferred = $q.defer();

      if (!task) {
        return deferred.resolve(null);
      }

      History.userOperation({taskId : task.id}, function(err, res) {
        if(err) {
          deferred.reject(err);
        }
        else {
          deferred.resolve(res);
        }
      });

      return deferred.promise;
    }]);

    historyData.provide('comments', ['task', function(task) {
      var deferred = $q.defer();

      if (!task) {
        return deferred.resolve(null);
      }

      Task.comments(task.id, function(err, res) {
        if(err) {
          deferred.reject(err);
        }
        else {
          deferred.resolve(res);
        }
      });

      return deferred.promise;
    }]);

    historyData.provide('orderedHistoryAndCommentsByDay', ['history', 'comments', function(history, comments) {
      history = history || {};
      comments = comments || {};

      var days = [],
          i = 0,
          day;

      for (var historyEvent; (historyEvent = history[i]); i++) {
        // create object for each day, containing the events for this day
        day = findOrCreateDay(days, historyEvent.timestamp);

        // create historyEvent object for each operationId
        var parentEvent = findOrCreateParentEvent(day.events, historyEvent);

        // preprocess the dates to avoid function calls from the template
        if (isTimestampProperty(historyEvent.property)) {
          historyEvent.propertyIsDate = true;
          historyEvent.newValue = historyEvent.newValue ? parseInt(historyEvent.newValue, 10) : null;
          historyEvent.orgValue = historyEvent.orgValue ? parseInt(historyEvent.orgValue, 10) : null;
        }

        parentEvent.subEvents.push(historyEvent);

      }

      // reset values
      i = 0;
      day = null;

      for (var comment; (comment = comments[i]); i++) {
        day = findOrCreateDay(days, comment.time);
        comment.type = 'Comment';
        day.events.push(comment);
      }

      return days;
    }]);

    $scope.state = historyData.observe('orderedHistoryAndCommentsByDay', function(days) {
      $scope.days = days;
    });

  }];

var Configuration = function PluginConfiguration(ViewsProvider) {

  ViewsProvider.registerDefaultView('tasklist.task.detail', {
    id: 'task-detail-history',
    label: 'HISTORY',
    template: template,
    controller: Controller,
    priority: 800
  });
};

Configuration.$inject = ['ViewsProvider'];

module.exports = Configuration;
