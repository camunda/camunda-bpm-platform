'use strict';

var Table = require('./../../table');

module.exports = Table.extend({

  tabRepeater: 'tabProvider in processDefinitionTabs',
  tabIndex: 0,
  tabLabel: 'Process Instances',
  tableRepeater: 'processInstance in processInstances',


  isInstanceSuspended: function(idx) {
    return this.tableItem(idx, '.state .badge-suspended').isDisplayed();
  },

  selectInstanceId: function(idx) {
    this.tableItem(idx, by.binding('processInstance.id')).click();
  },

  instanceId: function(idx) {
    return this.tableItem(idx, '[title]').getAttribute('title');
  },

  startTime: function(idx) {
    return this.tableItem(idx, '.start-time');
  },

  businessKey: function(idx) {
    return this.tableItem(idx, '.business-key');
  }

});
