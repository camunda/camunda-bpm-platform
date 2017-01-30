'use strict';

var angular = require('camunda-commons-ui/vendor/angular');

module.exports = [
  'camAPI', 'createListQueryFunction',
  function(camAPI, createListQueryFunction) {
    var externalTasks = camAPI.resource('external-task');
    var getExternalTasks = createListQueryFunction(
      externalTasks.count.bind(externalTasks),
      externalTasks.list.bind(externalTasks)
    );

    return {
      getActiveExternalTasksForProcess: getActiveExternalTasksForProcess
    };

    function getActiveExternalTasksForProcess(processId, pages, otherParams) {
      return getExternalTasks(
        angular.extend(
          {
            processInstanceId: processId
          },
          otherParams
        ),
        pages
      );
    }
  }
];
