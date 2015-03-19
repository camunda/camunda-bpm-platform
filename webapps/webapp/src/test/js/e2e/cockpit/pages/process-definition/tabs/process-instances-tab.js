'use strict';

var Table = require('./../../table');

module.exports = Table.extend({

  tabRepeater: 'tabProvider in processDefinitionTabs',
  tabIndex: 0,
  tabLabel: 'Process Instances',
  tableRepeater: 'processInstance in processInstances',

  selectInstance: function(item) {
    this.tableItem(item, 'processInstance.id').click();
  },

  instanceId: function(item) {
    return this.table().get(item).element(by.css('[title]')).getAttribute('title');
  },

  instanceStartTime: function(item) {
    return this.tableItem(item, 'processInstance.startTime').getText();
  },

  instanceBusinessKey: function(item) {
    return this.tableItem(item, 'processInstance.businessKey').getText();
  }

});
