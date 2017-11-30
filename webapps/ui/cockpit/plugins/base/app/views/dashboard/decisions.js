'use strict';
module.exports = [ 'ViewsProvider', function(ViewsProvider) {
  ViewsProvider.registerDefaultView('cockpit.navigation', {
    id: 'decisions',
    label: 'COCKPIT_DECISIONS',
    pagePath: '#/decisions',
    checkActive: function(path) {
      return path.indexOf('#/decision') > -1;
    },
    template: '<!-- nothing to show, but needed -->',
    controller: function() {},
    priority: 90
  });
}];
