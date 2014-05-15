'use strict';
if (typeof define !== 'function') { var define = require('amdefine')(module); }
/* jshint unused: false */
define([
           'angular', 'moment', 'camunda-tasklist/task/data', 'camunda-tasklist/form/data', 'angular-bootstrap'
], function(angular,   moment) {
  var taskModule = require('angular').module('cam.tasklist.task', [
    'cam.tasklist.task.data',
    'cam.tasklist.form.data',
    'ui.bootstrap',
    'cam.form',
    'angularMoment'
  ]);

  var c = 0;

  taskModule.directive('camTasklistTask', [
          '$modal', '$rootScope',
  function($modal,   $rootScope) {
    $rootScope.batchActions = {};
    $rootScope.batchActions.selected = [];

    return {
      link: function(scope, element) {
        scope.task = scope.task || $rootScope.focusedTask;

        scope.elUID = c;
        c++;

        element.find('.nav li').eq(0).addClass('active');
        element.find('.tab-pane').eq(0).addClass('active');

        $rootScope.$on('tasklist.task.focused', function() {
          scope.task = $rootScope.focusedTask;
          element.find('[data-toggle="tooltip"]').tooltip();
        });
      },
      templateUrl: 'scripts/task/task.html'
    };
  }]);

  // should be moved to the form module...
  taskModule.directive('camTasklistTaskForm', [
          'camTaskFormData', '$rootScope',
  function(camTaskFormData,   $rootScope) {
    return {
      link: function(scope, element) {
        scope.elUID = c;
        c++;

        scope.labelsWidth = 3;
        scope.fieldsWidth = 12 - scope.labelsWidth;

        $rootScope.$on('tasklist.task.focused', function() {
          scope.fields = camTaskFormData();
          element.find('[data-toggle="tooltip"]').tooltip();
        });
      },
      templateUrl: 'scripts/form/form.html'
    };
  }]);

  taskModule.directive('camTasklistTaskHistory', [
          'camTaskHistoryData', '$rootScope',
  function(camTaskHistoryData,   $rootScope) {
    return {
      link: function(scope, element) {
        scope.history = [];
        scope.days = [];

        $rootScope.$on('tasklist.task.focused', function() {
          scope.history = camTaskHistoryData(null, null);
          scope.now = new Date();
          var days = {};
          angular.forEach(scope.history, function(event) {
            var mom = moment(event.timestamp, 'X');
            var date = mom.format('DD-MMMM-YYYY');
            var time = mom.format('HH:mm');
            var parts = date.split('-');
            days[date] = days[date] || {
              date: {
                day: parts[0],
                month: parts[1],
                year: parts[2]
              },
              events: {}
            };
            days[date].events[time] = days[date].events[time] || [];
            days[date].events[time].push(event);
          });
          scope.days = days;
          setTimeout(function() {
            element.find('[data-toggle="tooltip"]').tooltip();
          }, 10);
        });
      },
      templateUrl: 'scripts/task/history.html'
    };
  }]);

  return taskModule;
});
