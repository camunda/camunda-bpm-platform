'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/external-tasks-tab.html', 'utf8');

module.exports = function() {
  return {
    restrict: 'A',
    template: template,
    transclude: true,
    controller: 'ExternalTasksTabController as TasksTab',
    scope: {
      onLoad: '&externalTasksTab',
      processData: '='
    }
  };
};
