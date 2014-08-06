define([
  'text!./cam-tasklist-task-form.html'
], function(template) {
  'use strict';

  return [
    'camAPI',
  function(
    camAPI
  ) {
    var Task = camAPI.resource('task');

    return {
      scope: {
        task: '='
      },
      link: function(scope) {
        console.info('scope.task', scope.task);
        scope.currentTaskURL = null;


        function loadForm() {
          var url = scope.task.formKey;
          url = '/camunda'+ url.split(':').pop();
          console.info('load form from '+ url);
          Task.http.load(url, {
            done: processForm
          });
        }

        function processForm(err, resp) {
          if (err) {
            throw err;
          }

          console.info(resp);
        }

        scope.$watch('task', function() {
          if (!scope.task) {
            scope.currentTaskURL = null;
          }
          else if (scope.currentTaskURL !== scope.task._links.self.href) {
            scope.currentTaskURL = scope.task._links.self.href;

            loadForm();
          }
        });

        if (scope.task) {
          scope.currentTaskURL = scope.task._links.self.href;

          loadForm();
        }
      },
      template: template
    };
  }];
});
