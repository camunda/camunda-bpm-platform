'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/decision-list.html', 'utf8');

module.exports = [ 'ViewsProvider', function(ViewsProvider) {
  ViewsProvider.registerDefaultView('cockpit.decisions.dashboard', {
    id: 'decision-list',
    label: 'Deployed Decision Tables',
    template: template,
    controller: 'DecisionListController',
    priority: -5 // display below the process definition list
  });
}];
