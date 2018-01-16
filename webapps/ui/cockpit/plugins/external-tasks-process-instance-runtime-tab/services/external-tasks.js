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

    function getActiveExternalTasksForProcess(processId, pages, sorting, otherParams) {
      var countParams = { processInstanceId : processId };
      var sortParams = { sorting: [ sorting ] };
      return getExternalTasks(angular.extend(countParams, otherParams), pages, sortParams);
    }
  }
];
