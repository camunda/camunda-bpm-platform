'use strict';

var Base = require('./../table');
var repeater = 'tabProvider in processInstanceTabs';
var tabIndex = 2;

module.exports = Base.extend({

  selectCalledProcessInstanceTab: function() {
    this.selectTab(repeater, tabIndex);
  },

  calledProcessInstanceTabName: function() {
    return this.tabName(repeater, tabIndex);
  },

  isCalledProcessInstanceTabSelected: function() {
    expect(this.isTabSelected(repeater, tabIndex)).toMatch('ng-scope active');
  },

  isCalledProcessInstanceTabNotSelected: function() {
    expect(this.isTabSelected(repeater, tabIndex)).not.toMatch('ng-scope active');
  },

  calledProcessInstancesTable: function() {
    this.selectCalledProcessInstanceTab();
    return element.all(by.repeater('calledProcessInstance in calledProcessInstances'));
  },

  selectVariables: function(item) {
    this.calledProcessInstancesTable().get(item).element(by.binding('calledProcessInstance.id')).click();
  }

});