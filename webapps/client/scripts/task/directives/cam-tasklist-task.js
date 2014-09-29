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
        scope.task = scope.task || $rootScope.currentTask;

        scope.tabs = {
          form: true,
          description: false,
          history: false,
          diagram: false
        };

        scope.elUID = camUID();

        // element.find('.nav li').eq(0).addClass('active');
        // element.find('.tab-pane').eq(0).addClass('active');

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
          Task.get(taskId, function(err, task) {
            if (err) { throw err; }


            task.due = fixReadDateTimezone(task.due);
            task.followUp = fixReadDateTimezone(task.followUp);


            scope.task = $rootScope.currentTask = task;
            refreshTabs();
          });
        }


        function setTask(event) {
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


          var state = $location.search();
          state.tab = state.tab = tabName;
          $location.search(state);
          // scope.tabs[tabName] = true;
        };

        scope.fullscreenForm = function() {
          element.find('[cam-tasklist-task-form]').isolateScope().enterFullscreen();
        };

        scope.drawDiagram = function() {
          element.find('[cam-tasklist-task-diagram]').isolateScope().drawDiagram();
        };

        scope.$on('tasklist.task.current', setTask);

        $rootScope.$on('$locationChangeSuccess', setTask);

        setTask();
      }
    };
  }];
});
