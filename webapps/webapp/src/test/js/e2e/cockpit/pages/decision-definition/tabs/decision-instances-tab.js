'use strict';

var Table = require('./../../table');

module.exports = Table.extend({

  tabRepeater: 'tabProvider in decisionDefinitionTabs',
  tabIndex: 0,
  tabLabel: 'Decision Instances',
  tableRepeater: 'decisionInstance in decisionInstances',

  selectInstanceId: function(idx) {
    this.tableItem(idx, by.binding('decisionInstance.id')).click();
  },

  selectProcessDefinitionKey: function(idx) {
    this.tableItem(idx, by.binding('decisionInstance.processDefinitionKey')).click();
  },

  selectActivityId: function(idx) {
    this.tableItem(idx, by.binding('decisionInstance.activityId')).click();
  },

  instanceId: function(idx) {
    return this.tableItem(idx, '[title]').getAttribute('title');
  },

});
