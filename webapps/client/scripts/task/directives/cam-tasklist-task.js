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

        function loadTask(taskId) {
          // wait for #CAM-2596
          Task.get(taskId, function(err, task) {
            if (err) { throw err; }
            scope.task = $rootScope.currentTask = task;
          });
        }

        function setTask(event) {
          var urlTaskId = $location.search().task;

          if ($rootScope.currentTask) {
            if (urlTaskId && urlTaskId === $rootScope.currentTask.id) {
              scope.task = $rootScope.currentTask;
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
          }
        }

        scope.fullscreenForm = function() {
          element.find('[cam-tasklist-task-form]').scope().enterFullscreen();
        };

        scope.$on('tasklist.task.current', setTask);

        $rootScope.$on('$locationChangeSuccess', setTask);

        setTask();
      }
    };
  }];
});
