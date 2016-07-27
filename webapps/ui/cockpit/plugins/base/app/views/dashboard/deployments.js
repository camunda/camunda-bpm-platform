'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/deployments.html', 'utf8');

module.exports = [ 'ViewsProvider', function(ViewsProvider) {
  ViewsProvider.registerDefaultView('cockpit.dashboard.section', {
    id: 'deployments',
    label: 'Deployments',
    template: template,
    pagePath: '#/repository',
    controller: [function() {}],

    priority: -5
  });
}];
