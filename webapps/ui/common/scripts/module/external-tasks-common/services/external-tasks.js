'use strict';

var angular = require('camunda-commons-ui/vendor/angular');

module.exports = [
  '$q', 'camAPI',
  function($q, camAPI) {
    var externalTasks = camAPI.resource('external-task');

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

    function getExternalTasks(query, pages) {
      return externalTasks
        .count(query)
        .then(function(data) {
          var first = (pages.current - 1) * pages.size;
          var count = data.count;
          var listQuery = angular.extend(
            {},
            query,
            {
              firstResult: first,
              maxResults: pages.size
            }
          );

          if (count > first) {
            return $q.all({
              count: count,
              tasks: externalTasks.list(listQuery)
            });
          }

          return data;
        });
    }
  }
];
