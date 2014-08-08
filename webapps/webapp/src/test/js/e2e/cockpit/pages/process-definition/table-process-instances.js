'use strict';

var Table = require('./../table');

module.exports = Table.extend({

  repeater: 'tabProvider in processDefinitionTabs',
  tabIndex: 0,

  selectProcessInstanceTab: function() {
    this.selectTab();
  },

  processInstanceTabName: function() {
    return this.tabName();
  },

  isProcessInstanceTabSelected: function() {
    expect(this.isTabSelected()).toMatch('ng-scope active');
  },

  isProcessInstanceTabNotSelected: function() {
    expect(this.isTabSelected()).not.toMatch('ng-scope active');
  },

  processInstanceTable: function() {
    this.selectProcessInstanceTab();
    return element.all(by.repeater('processInstance in processInstances'));
  },

  selectProcessInstance: function(item) {
    this.processInstanceTable().get(item).element(by.binding('processInstance.id')).click();
  },

  processInstanceName: function(item) {
    return this.processInstanceTable().get(item).element(by.css('[title]')).getAttribute('title');
  }

});
