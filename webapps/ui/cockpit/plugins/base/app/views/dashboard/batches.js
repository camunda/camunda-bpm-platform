'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/batches.html', 'utf8');

module.exports = [ 'ViewsProvider', function(ViewsProvider) {
  ViewsProvider.registerDefaultView('cockpit.dashboard.section', {
    id: 'batch',
    label: 'Batches',
    template: template,
    pagePath: '#/batch',
    controller: [function() {}],

    priority: -5
  });
}];
