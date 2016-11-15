'use strict';
module.exports = [
  'ViewsProvider',
  function(
    ViewsProvider
  ) {
    ViewsProvider.registerDefaultView('cockpit.navigation', {
      id: 'tasks',
      label: 'Human Tasks',
      template: '<!-- nothing to show, but needed -->',
      pagePath: '#/tasks',
      checkActive: function(path) {
        return path.indexOf('#/tasks') > -1;
      },
      controller: function() {},

      priority: 20
    });
  }];