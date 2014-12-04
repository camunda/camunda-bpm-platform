'use strict';

var Table = require('./../../table');

module.exports = Table.extend({

  tabRepeater: 'tabProvider in processDefinitionTabs',
  tabIndex: 0,
  tabLabel: 'Process Instances',
  tableRepeater: 'processInstance in processInstances',

  selectProcessInstance: function(item) {
    this.tableItem(item, 'processInstance.id').click();
  },

  processInstanceId: function(item) {
    return this.table().get(item).element(by.css('[title]')).getAttribute('title');
  },

  processInstanceStartTime: function(item) {
    return this.tableItem(item, 'processInstance.startTime').getText();
  },

  processInstanceBusinessKey: function(item) {
    return this.tableItem(item, 'processInstance.businessKey').getText();
  }

});
