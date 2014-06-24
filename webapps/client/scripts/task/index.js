'use strict';
if (typeof define !== 'function') { var define = require('amdefine')(module); }
/* jshint unused: false */
define([
           'angular', 'moment',
           'camunda-tasklist-ui/utils',
           'camunda-tasklist-ui/api',
           'angular-bootstrap',
           'text!camunda-tasklist-ui/task/task.html',
           'text!camunda-tasklist-ui/task/form.html',
           'text!camunda-tasklist-ui/task/history.html'
], function(angular,   moment) {
  var taskModule = angular.module('cam.tasklist.task', [
    require('camunda-tasklist-ui/utils').name,
    require('camunda-tasklist-ui/api').name,
    'ui.bootstrap',
    'cam.form',
    'angularMoment'
  ]);

  /**
   * @module cam.tasklist.task
   */

  /**
   * @memberof cam.tasklist
   */

  taskModule.directive('camTasklistTask', [
          '$modal', '$rootScope', 'camUID',
  function($modal,   $rootScope,   camUID) {
    $rootScope.batchActions = {};
    $rootScope.batchActions.selected = [];

    return {
      link: function(scope, element) {
        scope.task = scope.task || $rootScope.currentTask;

        scope.elUID = camUID();

        element.find('.nav li').eq(0).addClass('active');
        element.find('.tab-pane').eq(0).addClass('active');

        $rootScope.$watch('currentTask', function() {
          if (!$rootScope.currentTask || (scope.task && scope.task.id === $rootScope.currentTask.id)) {
            return;
          }
          scope.task = $rootScope.currentTask;
          console.info('Current task is now', scope.task);
        });
      },
      template: require('text!camunda-tasklist-ui/task/task.html')
    };
  }]);

  // should be moved to the form module...
  taskModule.directive('camTasklistTaskForm', [
          'camAPI', '$rootScope', 'camUID',
  function(camAPI,   $rootScope,   camUID) {
    return {
      link: function(scope, element) {
        scope.task = scope.task || $rootScope.currentTask;

        scope.elUID = camUID();

        scope.labelsWidth = 3;
        scope.fieldsWidth = 12 - scope.labelsWidth;

        $rootScope.$watch('currentTask', function() {
          if (!$rootScope.currentTask || (scope.task && scope.task.id === $rootScope.currentTask.id)) {
            return;
          }
          scope.task = $rootScope.currentTask;
          console.info('Current task is now, get the form', scope.task);
          // scope.fields = camTaskFormData();
        });
      },
      template: require('text!camunda-tasklist-ui/task/form.html')
    };
  }]);

  taskModule.directive('camTasklistTaskHistory', [
          'camAPI', '$rootScope',
  function(camAPI,   $rootScope) {
    return {
      link: function(scope, element) {
        scope.history = [];
        scope.days = [];

        $rootScope.$on('tasklist.task.current', function() {
          // scope.history = camTaskHistoryData(null, null);
          scope.history = [];
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
        });
      },
      template: require('text!camunda-tasklist-ui/task/history.html')
    };
  }]);

  return taskModule;
});
