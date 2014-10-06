define([
  'text!./cam-tasklist-task.html'
], function(template) {
  'use strict';

  function fixReadDateTimezone(dateStr) {
    if (!dateStr) { return dateStr; }
    var d = new Date(dateStr);
    return (new Date(d.getTime() + (d.getTimezoneOffset() * 60 * 1000))).toJSON();
  }

  return [
    '$rootScope',
    '$location',
    'camUID',
    'camAPI',
  function(
    $rootScope,
    $location,
    camUID,
    camAPI
  ) {
    var Task = camAPI.resource('task');

    $rootScope.batchActions = {};
    $rootScope.batchActions.selected = [];

    return {
      scope: {
        task: '='
      },

      template: template,

      link: function(scope, element) {
        var _scopeEvents = [];
        element.on('$destroy', function() {
          if (!_scopeEvents.length) { return; }
          angular.forEach(_scopeEvents, function(fn) { fn(); });
        });

        scope.tabs = {
          form: true,
          description: false,
          history: false,
          diagram: false
        };

        scope.elUID = camUID();

        function refreshTabs() {
          var activePane = element.find('.tab-pane.active > div');
          var paneScope = activePane.hasClass('ng-isolated-scope') ?
                          activePane.isolateScope() :
                          activePane.scope();

          if (activePane.hasClass('diagram-pane')) {
            paneScope.drawDiagram();
          }
        }


        function loadTask(taskId) {
          Task.get(taskId, function(err, loadedTask) {
            if (err) { throw err; }


            loadedTask.due = fixReadDateTimezone(loadedTask.due);
            loadedTask.followUp = fixReadDateTimezone(loadedTask.followUp);

            scope.task = loadedTask;
            if (!$rootScope.currentTask || $rootScope.currentTask.id !== loadedTask.id) {
              $rootScope.currentTask = loadedTask;
            }
            refreshTabs();
          });
        }


        function setTask(newTask) {
          if (newTask && newTask.id) {
            if (!$rootScope.currentTask || $rootScope.currentTask.id !== newTask.id) {
              $rootScope.currentTask = newTask;
            }
            scope.task = newTask;
            return;
          }

          var state = $location.search();
          var urlTaskId = state.task;

          if ($rootScope.currentTask) {
            if (urlTaskId && urlTaskId === $rootScope.currentTask.id) {
              scope.task = $rootScope.currentTask;
              refreshTabs();
            }
            else if (urlTaskId) {
              loadTask(urlTaskId);
            }
          }
          else if(urlTaskId) {
            loadTask(urlTaskId);
          }
          else {
            // should we load the next task?
            // that would happen here
            scope.task = null;
            refreshTabs();
          }
        }

        scope.selectTab = function(tabName) {
          if (tabName === 'diagram') {
            scope.drawDiagram();
          }
        };

        scope.fullscreenForm = function() {
          element.find('[cam-tasklist-task-form]').isolateScope().enterFullscreen();
        };

        scope.drawDiagram = function() {
          element.find('[cam-tasklist-task-diagram]').isolateScope().drawDiagram();
        };

        scope.$on('tasklist.task.current', function(ev, task) {
          setTask(task);
        });

        setTask($rootScope.currentTask);
      }
    };
  }];
});
