'use strict';
var fs = require('fs');

var template = fs.readFileSync(__dirname + '/cam-tasklist-form-external.html', 'utf8');

module.exports = [
  '$location',
  function($location) {

    return {

      restrict: 'A',

      require: '^camTasklistForm',

      scope: true,

      template: template,

      link : function($scope, $elment, attrs, formController) {

        formController.notifyFormValidated(true);

        $scope.externalFormUrl  = null;
        $scope.EXTERNAL_FORM_NOTE = null;

        $scope.$watch(function() {
          return formController.getTasklistForm() && formController.getParams();
        }, function(value) {

          if (value) {

            var tasklistForm = formController.getTasklistForm();
            var params = formController.getParams();

            var key = tasklistForm.key;

            var taskId = params.taskId;
            var processDefinitionKey = params.processDefinitionKey;

            var queryParam = null;

            if (taskId) {
              queryParam = 'taskId=' + taskId;
              $scope.EXTERNAL_FORM_NOTE = 'TASK_EXTERNAL_FORM_NOTE';

            } else if (processDefinitionKey) {
              queryParam = 'processDefinitionKey=' + processDefinitionKey;
              $scope.EXTERNAL_FORM_NOTE = 'PROCESS_EXTERNAL_FORM_NOTE';

            } else {
              return formController.notifyFormInitializationFailed({
                message: 'INIT_EXTERNAL_FORM_FAILED'
              });
            }

            var absoluteUrl = $location.absUrl();
            var url = $location.url();

            // remove everthing after '#/', e.g.:
            // '.../#/?task=abc&...' ---> '.../#/'
            absoluteUrl = absoluteUrl.replace(url, '/');

            $scope.externalFormUrl  = encodeURI(key + '?' + queryParam + '&callbackUrl=' + absoluteUrl);

            formController.notifyFormInitialized();
          }

        });

        $scope.$watch(function() {
          return formController.getOptions();
        }, function(options) {

          if (options) {
            options.hideCompleteButton = true;
          }

        });

      }

    };

  }];
