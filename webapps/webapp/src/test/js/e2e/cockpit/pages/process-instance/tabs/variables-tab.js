'use strict';

var Table = require('./../../table');

module.exports = Table.extend({

  tabRepeater: 'tabProvider in processInstanceTabs',
  tabIndex: 0,
  tabLabel: 'Variables',
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
  },

  inlineEditRow: function() {
    return element(by.css('.editing'));
  },

  editVariableButton: function(item) {
    return this.tableItem(item, 'variable.value').element(by.css('[ng-click="editVariable(variable)"]'));
  },

  editVariableValue: function(inputValue) {
    var inputField = this.inlineEditRow().element(by.model('variable.value'));

    if (arguments.length !== 0)
      inputField.sendKeys(inputValue);

    return inputField;
  },

  editVariableType: function(type) {
    this.inlineEditRow().element(by.cssContainingText('option', type)).click();
  },

  editVariableErrorText: function() {
    return this.inlineEditRow().element(by.css('.invalid:not(.ng-hide)')).getText();
  },

  editVariableConfirmButton: function() {
    return this.inlineEditRow().element(by.css('.inline-edit-footer .btn-primary'));
  },

  editVariableCancelButton: function() {
    return this.inlineEditRow().element(by.css('[ng-click="closeInPlaceEditing(variable)"]'));
  }

});
