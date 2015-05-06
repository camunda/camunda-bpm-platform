'use strict';

var Table = require('./../../table');

module.exports = Table.extend({

  tabRepeater: 'tabProvider in processInstanceTabs',
  tabIndex: 0,
  tabLabel: 'Variables',
  tableRepeater: 'variable in variables',

  variableName: function(item) {
    return this.tableItem(item, '.variable-name');
  },

  variableValue: function(item) {
    return this.tableItem(item, '.variable-value');
  },

  variableType: function(item) {
    return this.tableItem(item, '.variable-type');
  },

  variableScope: function(item) {
    return this.tableItem(item, '.variable-scope');
  },

  inlineEditRow: function() {
    return element(by.css('.editing'));
  },

  editVariableButton: function(item) {
    return this.variableValue(item).element(by.css('[ng-click="editVariable(variable)"]'));
  },

  editVariableInput: function(inputValue) {
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
