'use strict';

var Base = require('./../table');

module.exports = Table.extend({

  tabRepeater: 'tabProvider in processInstanceTabs',
  tabIndex: 2,
  tableRepeater: 'calledProcessInstance in calledProcessInstances',

  calledProcessInstanceName: function(item) {
    return this.table().get(item).element(by.css('[title]')).getAttribute('title').getText();
  },

  selectCalledProcessInstance: function(item) {
    return this.tableItem(item, 'calledProcessInstance.id').click();
  },

  processDefinitionName: function(item) {
    return this.tableItem(item, 'calledProcessInstance.processDefinitionName').getText();
  },

  selectProcessDefinition: function(item) {
    return this.tableItem(item, 'calledProcessInstance.processDefinitionName').click();
  },

  calledActivityName: function(item) {
    return this.tableItem(item, 'calledProcessInstance.instance.name').getText();
  },

  selectCalledActivity: function(item) {
    return this.tableItem(item, 'calledProcessInstance.instance.name').click();
  }

});