'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/process-instance-runtime-external-tasks.html', 'utf8');

module.exports = ['ViewsProvider', function(ViewsProvider) {
  ViewsProvider.registerDefaultView('cockpit.processInstance.runtime.tab', {
    id: 'external-tasks-process-instance-runtime',
    label: 'External Tasks',
    template: template,
    controller: 'ProcessInstanceRuntimeTabController as RuntimeTab'
  });
}];
