'use strict';

var Base = require('./../table');
var repeater = 'tabProvider in processInstanceTabs';
var tabIndex = 0;

module.exports = Base.extend({

  selectVariablesTab: function() {
    this.selectTab(repeater, tabIndex);
  },

  variablesTabName: function() {
    return this.tabName(repeater, tabIndex);
  },

  isVariablesTabSelected: function() {
    expect(this.isTabSelected(repeater, tabIndex)).toMatch('ng-scope active');
  },

  isVariablesTabNotSelected: function() {
    expect(this.isTabSelected(repeater, tabIndex)).not.toMatch('ng-scope active');
  },

  calledProcessInstancesTable: function() {
    this.selectVariablesTab();
    return element.all(by.repeater('variable in variables'));
  },

  selectVariables: function(item) {
    this.calledProcessInstancesTable().get(item).element(by.binding('variable.name')).click();
  }

});