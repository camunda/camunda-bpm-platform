'use strict';

var Table = require('./../../table');

var Variable = require('camunda-commons-ui/lib/widgets/variables-table/test/cam-widget-variables-table.page').Variable;


module.exports = Table.extend({

  tabRepeater: 'tabProvider in processInstanceTabs',
  tabIndex: 0,
  tabLabel: 'Variables',
  tableRepeater: '(v, info) in variables',


  //------------------------------------------------------------------

  variableAt: function (index) {
    if (typeof index !== 'number') {
      return new Variable(index);
    }
    return new Variable(this.tableItem(index));
  },


  variableByName: function (varName) {
    return new Variable(element(by.cssContainingText('td.col-name', varName)).element(by.xpath('..')));
  },


  //------------------------------------------------------------------


  variableName: function(item) {
    return this.tableItem(item, '.col-name');
  },

  variableValue: function(item) {
    return this.tableItem(item, '.col-value');
  },

  variableType: function(item) {
    return this.tableItem(item, '.col-type');
  },

  variableScope: function(item) {
    return this.tableItem(item, '.col-scope');
  },

  variableScopeLink: function(item) {
    return this.variableScope(item).element(by.css('a'));
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
  },

  deleteVariable: function (index) {
    var varObj;
    if (typeof index === 'string') {
      varObj = element(by.cssContainingText('td.col-name', index)).element(by.xpath('..'));
    }
    else {
      varObj = this.variableAt(index);
    }
    return varObj.deleteButton().click();
  }

});
