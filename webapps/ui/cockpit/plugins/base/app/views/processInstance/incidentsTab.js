'use strict';

var fs = require('fs');

var incidentsTemplate = fs.readFileSync(__dirname + '/incidents-tab.html', 'utf8');

var Configuration = function PluginConfiguration(ViewsProvider) {
  ViewsProvider.registerDefaultView('cockpit.processInstance.runtime.tab', {
    id: 'incidents-tab',
    label: 'Incidents',
    template: incidentsTemplate,
    priority: 15
  });
};

Configuration.$inject = ['ViewsProvider'];

module.exports = Configuration;
