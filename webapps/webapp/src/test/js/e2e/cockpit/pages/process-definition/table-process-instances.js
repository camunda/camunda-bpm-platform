'use strict';

var Base = require('./../table');
var repeater = 'tabProvider in processDefinitionTabs';
var tabIndex = 0;

module.exports = Base.extend({

  selectProcessInstanceTab: function() {
    this.selectTab(repeater, tabIndex);
  },

  processInstanceTabName: function() {
    return this.tabName(repeater, tabIndex);
  },

  isProcessInstanceTabSelected: function() {
    expect(this.isTabSelected(repeater, tabIndex)).toMatch('ng-scope active');
  },

  isProcessInstanceTabNotSelected: function() {
    expect(this.isTabSelected(repeater, tabIndex)).not.toMatch('ng-scope active');
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
