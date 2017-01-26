'use strict';

var fs = require('fs');

var template = fs.readFileSync(__dirname + '/cam-tabs.html', 'utf8');

module.exports = function() {
  return {
    restrict: 'A',
    template: template,
    controller: 'CamTabsController as Tabs',
    scope: {
      providerParams: '=camTabs',
      tabsApi: '=?',
      vars: '=?',
      varsValues: '=?'
    }
  };
};
