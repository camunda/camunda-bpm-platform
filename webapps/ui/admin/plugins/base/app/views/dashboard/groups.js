'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/groups.html', 'utf8');

module.exports = [
  'ViewsProvider',
function (
  ViewsProvider
) {
  ViewsProvider.registerDefaultView('admin.dashboard.section', {
    id: 'groups',
    label: 'Groups',
    template: template,
    pagePath: '#/groups',
    controller: [function() {}],

    priority: 0
  });
}];
