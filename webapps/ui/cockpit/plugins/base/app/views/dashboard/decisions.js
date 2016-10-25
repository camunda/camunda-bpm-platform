'use strict';
module.exports = [ 'ViewsProvider', function(ViewsProvider) {
  ViewsProvider.registerDefaultView('cockpit.dashboard.section', {
    id: 'decisions',
    label: 'Decisions',
    pagePath: '#/decisions',
    checkActive: function(path) {
      return path.indexOf('#/decision') > -1;
    },
    template: '<!-- nothing to show, but needed -->',
    controller: function() {},
    priority: 30
  });
}];
