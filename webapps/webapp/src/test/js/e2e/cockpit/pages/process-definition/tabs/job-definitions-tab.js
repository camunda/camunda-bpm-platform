'use strict';

var Table = require('./../../table');

module.exports = Table.extend({

  tabRepeater: 'tabProvider in processDefinitionTabs',
  tabIndex: 2,
  tabLabel: 'Job Definitions',
  tableRepeater: 'jobDefinition in jobDefinitions',

  state: function(item) {
    return this.tableItem(item, '.state:not(.ng-hide)');
  },

  activity: function(item) {
    return this.tableItem(item, '.activity');
  },

  configuration: function(item) {
    return this.tableItem(item, '.configuration');
  },

  suspendJobDefinitionButton: function(item) {
    return this.tableItem(item, '[ng-click="openSuspensionStateDialog(jobDefinition)"]:not(.ng-hide)');
  },

  activateJobDefinitionButton: function(item) {
    return this.suspendJobDefinitionButton(item);
  }

});
