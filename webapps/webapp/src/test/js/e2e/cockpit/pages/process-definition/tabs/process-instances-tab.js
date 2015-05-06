'use strict';

var Table = require('./../../table');

module.exports = Table.extend({

  tabRepeater: 'tabProvider in processDefinitionTabs',
  tabIndex: 0,
  tabLabel: 'Process Instances',
  tableRepeater: 'processInstance in processInstances',

  instanceIdClick: function(item) {
    this.table().get(item).element(by.binding('processInstance.id')).click();
  },

  instanceId: function(item) {
    return this.tableItem(item, '[title]').getAttribute('title');
  },

  startTime: function(item) {
    return this.tableItem(item, '.start-time');
  },

  businessKey: function(item) {
    return this.tableItem(item, '.business-key');
  }

});
