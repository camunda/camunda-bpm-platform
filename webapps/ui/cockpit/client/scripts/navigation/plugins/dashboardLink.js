'use strict';

var fs = require('fs');

var dashboardLinkTemplate = fs.readFileSync(__dirname + '/dashboardLink.html', 'utf8');

var Configuration = function PluginConfiguration(ViewsProvider) {

  ViewsProvider.registerDefaultView('cockpit.navbar.action', {
    id: 'dashboard',
    template: dashboardLinkTemplate,
    priority: 200
  });
};

Configuration.$inject = ['ViewsProvider'];

module.exports = Configuration;
