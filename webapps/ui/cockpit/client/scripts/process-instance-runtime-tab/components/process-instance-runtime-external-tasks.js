'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/process-instance-runtime-external-tasks.html', 'utf8');

module.exports = function() {
  return {
    restrict: 'A',
    template: template,
    controller: 'ProcessInstanceRuntimeExternalTasksController as RuntimeTab'
  };
};
