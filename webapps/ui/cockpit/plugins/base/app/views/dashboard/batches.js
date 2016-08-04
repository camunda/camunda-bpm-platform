'use strict';

module.exports = [ 'ViewsProvider', function(ViewsProvider) {
  ViewsProvider.registerDefaultView('cockpit.dashboard.section', {
    id: 'batch',
    label: 'Batches',
    pagePath: '#/batch',
    template: '<!-- nothing to show, but needed -->',
    controller: function() {},

    priority: -5
  });
}];
