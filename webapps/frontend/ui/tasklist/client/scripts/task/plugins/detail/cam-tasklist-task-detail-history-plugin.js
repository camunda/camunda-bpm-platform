/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

'use strict';
var fs = require('fs');

var template = require('./cam-tasklist-task-detail-history-plugin.html')();

var jquery = require('jquery');
var moment = require('../../../../../../../camunda-commons-ui/vendor/moment');

var findOrCreateDay = function(days, timestamp) {
  var day = jquery.grep(days, function(elem) {
    return (
      moment(elem.date, moment.ISO_8601).format('YYYY-MM-DD') ===
      moment(timestamp, moment.ISO_8601).format('YYYY-MM-DD')
    );
  });
  if (day.length > 0) {
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
  if (parentEvent.length > 0) {
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
  function($scope, camAPI, $q) {
    var History = camAPI.resource('history');
    var Task = camAPI.resource('task');

    var historyData = $scope.taskData.newChild($scope);

    var pages = ($scope.pages = {
      size: 50,
      total: 0,
      current: 1
    });

    $scope.onPaginationChange = function onPaginationChange() {
      historyData.changed('history');
    };

    historyData.provide('history', [
      'task',
      function(task) {
        var deferred = $q.defer();

        if (!task) {
          return deferred.resolve(null);
        }

        History.userOperationCount(
          {
            taskId: task.id
          },
          function(err, res) {
            if (err) {
              throw err;
            } else {
              pages.total = res.count;
            }
          }
        );

        History.userOperation(
          {
            taskId: task.id,
            maxResults: pages.size,
            firstResult: pages.size * (pages.current - 1)
          },
          function(err, res) {
            if (err) {
              deferred.reject(err);
            } else {
              deferred.resolve(res);
            }
          }
        );

        return deferred.promise;
      }
    ]);

    historyData.provide('comments', [
      'task',
      function(task) {
        var deferred = $q.defer();

        if (!task) {
          return deferred.resolve(null);
        }

        return Task.comments(task.id).catch(function() {});
      }
    ]);

    historyData.provide('orderedHistoryAndCommentsByDay', [
      'history',
      'comments',
      function(history, comments) {
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
            historyEvent.newValue = historyEvent.newValue
              ? parseInt(historyEvent.newValue, 10)
              : null;
            historyEvent.orgValue = historyEvent.orgValue
              ? parseInt(historyEvent.orgValue, 10)
              : null;
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
      }
    ]);

    $scope.state = historyData.observe(
      'orderedHistoryAndCommentsByDay',
      function(days) {
        $scope.days = days;
      }
    );
  }
];

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
