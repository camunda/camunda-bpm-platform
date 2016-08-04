'use strict';

module.exports = [ 'ViewsProvider', function(ViewsProvider) {
  ViewsProvider.registerDefaultView('cockpit.dashboard.section', {
    id: 'deployments',
    label: 'Deployments',
    pagePath: '#/repository',
    template: '<!-- nothing to show, but needed -->',
    controller: function() {},

    priority: -5
  });
}];
