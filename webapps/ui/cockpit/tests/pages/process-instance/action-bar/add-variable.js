'use strict';

var ActionBar = require('./../../action-bar');

module.exports = ActionBar.extend({

  barRepeater: 'tabProvider in processInstanceActions',

  addVariableButton: function() {
    return this.getActionButton(2);
  },

  modalHeading: function() {
    return element(by.css('.modal-header'));
  },

  variableNameInput: function(inputValue) {
    var inputField = element(by.css('.modal-body .variable-name')).element(by.model('newVariable.name'));

    if (arguments.length !== 0) {
      return inputField.sendKeys(inputValue);
    }
    return inputField;
  },

  variableTypeDropdown: function(type) {
    return element(by.css('.modal-body [ng-model="newVariable.type"]'))
              .element(by.cssContainingText('option', type));
  },

  variableTypeDropdownSelectedItem: function() {
    return element(by.css('.modal-body [ng-model="newVariable.type"] [selected="selected"]'));
  },

  variableValueInput: function(inputValue) {
    var inputField = element(by.css('.modal-body .variable-value')).element(by.model('variable.value'));

    if (arguments.length !== 0) {
      return inputField.sendKeys(inputValue);
    }
    return inputField;
  },

  variableValueRadio: function(value) {
    if (value) {
      return element(by.css('.modal-body .variable-value .radio [ng-value="true"]')).click();
    } else {
      return element(by.css('.modal-body .variable-value .radio [ng-value="false"]')).click();
    }
  },

  variableValueInfoLabel: function() {
    return element(by.css('.modal-body .variable-value .invalid:not(.ng-hide)'));
  },

  objectNameInput: function(inputValue) {
    var inputField = element(by.css('.modal-body .variable-value'))
                      .element(by.model('variable.valueInfo.objectTypeName'));

    if (arguments.length !== 0) {
      return inputField.sendKeys(inputValue);
    }
    return inputField;
  },

  objectFormatInput: function(inputValue) {
    var inputField = element(by.css('.modal-body .variable-value'))
                      .element(by.model('variable.valueInfo.serializationDataFormat'));

    if (arguments.length !== 0) {
      return inputField.sendKeys(inputValue);
    }
    return inputField;
  },

  objectValueInput: function(inputValue) {
    var inputField = element(by.css('.modal-body .variable-value'))
                      .element(by.model('variable.value'));

    if (arguments.length !== 0) {
      return inputField.sendKeys(inputValue);
    }
    return inputField;
  },

  addButton: function() {
    return element(by.css('[ng-click="save()"]'));
  },

  okButton: function() {
    return element(by.css('.modal-footer [ng-click="close()"]:not(.ng-hide)'));
  },

  addVariable: function(name, type, value) {
    var that = this;

    var submitFct = function() {
      that.addButton().click().then(function() {
        that.okButton().click();
      });
    };

    this.addVariableButton().click().then(function() {
      that.variableNameInput(name);
      that.variableTypeDropdown(type).click().then(function() {
        if(value) {
          if(typeof value === 'object') {
            that.objectNameInput(value.objectTypeName);
            that.objectFormatInput(value.serializationDataFormat);
            that.objectValueInput(value.value).then(submitFct);
          } else if(typeof value === 'boolean') {
            that.variableValueRadio(value).then(submitFct);
          } else {
            that.variableValueInput(value).then(submitFct);
          }
        } else {
          submitFct();
        }
      });
    });
  },


});
