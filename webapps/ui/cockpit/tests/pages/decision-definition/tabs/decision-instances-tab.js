'use strict';

var Table = require('./../../table');

module.exports = Table.extend({

  tabRepeater: 'tabProvider in decisionDefinitionTabs',
  tabIndex: 0,
  tabLabel: 'Decision Instances',
  tableRepeater: 'decisionInstance in decisionInstances',

  selectInstanceId: function(idx) {
    this.waitForElementToBeVisible(element(by.repeater(this.tableRepeater)));
    return this.tableItem(idx, by.binding('decisionInstance.id')).click();
  },

  selectProcessDefinitionKey: function(idx) {
    this.waitForElementToBeVisible(element(by.repeater(this.tableRepeater)));
    return this.tableItem(idx, by.binding('decisionInstance.processDefinitionKey')).click();
  },

  selectProcessInstanceId: function(idx) {
    this.waitForElementToBeVisible(element(by.repeater(this.tableRepeater)));
    return this.tableItem(idx, by.binding('decisionInstance.processInstanceId')).click();
  },

  instanceId: function(idx) {
    return this.tableItem(idx, '[title]').getAttribute('title');
  },



});
