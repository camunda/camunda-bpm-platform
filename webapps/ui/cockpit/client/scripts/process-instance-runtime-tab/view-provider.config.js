'use strict';

module.exports = ['ViewsProvider', function(ViewsProvider) {
  ViewsProvider.registerDefaultView('cockpit.processInstance.runtime.tab', {
    id: 'external-tasks-process-instance-runtime',
    label: 'External Tasks',
    template: '<div process-instance-runtime-external-tasks=""></div>'
  });
}];
