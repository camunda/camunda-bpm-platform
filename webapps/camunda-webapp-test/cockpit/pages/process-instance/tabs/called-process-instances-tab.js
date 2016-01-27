'use strict';

var Table = require('./../../table');

module.exports = Table.extend({

  tabRepeater: 'tabProvider in processInstanceTabs',
  tabIndex: 2,
  tabLabel: 'Called Process Instances',
  tableRepeater: 'calledProcessInstance in calledProcessInstances',

  calledProcessInstance: function(item) {
    return this.tableItem(item, '[title]').getAttribute('title').getText();
  },

  selectCalledProcessInstance: function(item) {
    return this.tableItem(item, '.called-process-instance').click();
  },

  processDefinition: function(item) {
    return this.tableItem(item, '.process-definition').getText();
  },

  selectProcessDefinition: function(item) {
    return this.tableItem(item, '.process-definition').click();
  },

  activity: function(item) {
    return this.tableItem(item, '.activity').getText();
  },

  selectActivity: function(item) {
    return this.tableItem(item, '.activity').click();
  }

});
