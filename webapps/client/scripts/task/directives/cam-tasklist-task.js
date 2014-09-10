define([
  'text!./cam-tasklist-task.html'
], function(template) {
  'use strict';

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
      template: template,

      link: function(scope, element) {
        scope.task = scope.task || $rootScope.currentTask;

        scope.elUID = camUID();

        element.find('.nav li').eq(0).addClass('active');
        element.find('.tab-pane').eq(0).addClass('active');

        function refreshTabs() {
          var activePane = element.find('.tab-pane.active > div');
          var paneScope = activePane.hasClass('ng-isolated-scope') ?
                          activePane.isolateScope() :
                          activePane.scope();

          console.info('paneScope', activePane[0], paneScope);

          if (activePane.hasClass('diagram-pane')) {
            paneScope.drawDiagram();
          }
        }

        function loadTask(taskId) {
          // wait for #CAM-2596
          Task.get(taskId, function(err, task) {
            if (err) { throw err; }
            scope.task = $rootScope.currentTask = task;
            refreshTabs();
          });
        }

        function setTask(event) {
          var urlTaskId = $location.search().task;

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
