'use strict';

var Base = require('./../table');

module.exports = Table.extend({

  tabRepeater: 'tabProvider in processInstanceTabs',
  tabIndex: 0,
  tableRepeater: 'variable in variables',

  variableName: function(item) {
    return this.tableItem(item, 'variable.name').getText();
  },

  variableValue: function(item) {
    return this.tableItem(item, 'variable.value').getText();
  },

  variableType: function(item) {
    return this.tableItem(item, 'variable.type').getText();
  },

  selectVariableScope: function(item) {
    this.tableItem(item, 'variable.instance.name').click();
  },

  variableScopeName: function(item) {
    return this.tableItem(item, 'variable.instance.name').getText();
  }

});