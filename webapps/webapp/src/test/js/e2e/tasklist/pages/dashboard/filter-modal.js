'use strict';

var Base = require('./../base');

module.exports = Base.extend({

  formElement: function() {
    return element(by.css('form[name="newFilter"]'));
  },

  formHeader: function() {
    return this.formElement().element(by.css('.modal-title')).getText();
  },

  selectPanel: function(panelItem) {
    var index = [
      'General',
      'Authorizations',
      'Criteria',
      'Variables'
    ];
    var item;
    var itemIndex = index.indexOf(panelItem) + 1;

    if (itemIndex)
      item = this.formElement().element(by.css('accordion .panel:nth-child(' + itemIndex + ') [ng-click="toggleOpen()"]'));
    else
      item = this.formElement().element(by.css('accordion .panel:nth-child(1) [ng-click="toggleOpen()"]'));

    item.click();
    return item;
  },

  // general
  nameInput: function(inputValue) {
    var inputField = this.formElement().element(by.model('filter.name'));

    if (arguments.length !== 0)
      inputField.sendKeys(inputValue);

    return inputField;
  },

  colorPicker: function(inputValue) {
    var inputField = this.formElement().element(by.model('filter.properties.color'));

    if (arguments.length !== 0)
      inputField.sendKeys(inputValue);

    return inputField;
  },

  descriptionInput: function(inputValue) {
    var inputField = this.formElement().element(by.model('filter.properties.description'));

    if (arguments.length !== 0)
      inputField.sendKeys(inputValue);

    return inputField;
  },

  priorityInput: function(inputValue) {
    var inputField = this.formElement().element(by.model('filter.properties.priority'));

    if (arguments.length !== 0)
      inputField.sendKeys(inputValue);

    return inputField;
  },

  // authorizations

  // criteria

  addCriterionButton: function() {
    return this.formElement().element(by.css('[ng-click="addCriterion()"]'));
  },

  keyInput: function(inputValue) {
    var inputField = this.formElement().element(by.model('queryParam.key'));

    if (arguments.length !== 0)
      inputField.sendKeys(inputValue);

    return inputField;
  },

  valueInput: function(inputValue) {
    var inputField = this.formElement().element(by.model('queryParam.value'));

    if (arguments.length !== 0)
      inputField.sendKeys(inputValue);

    return inputField;
  },

  // variables

  addVariableButton: function() {
    return this.formElement().element(by.css('[ng-click="addVariable()"]'));
  },

  variableList: function() {
    return this.formElement().all(by.repeater('(delta, variable) in _variables'));
  },

  removeVariableButton: function(item) {
    return this.variableList().get(item).element(by.css('[ng-click="removeVariable(delta)"]'));
  },

  variableNameInput: function(item, inputValue) {
    var inputField = this.variableList().get(item).element(by.model('variable.name'));

    if (arguments.length !== 0)
      inputField.sendKeys(inputValue);

    return inputField;
  },

  variableLabelInput: function(item, inputValue) {
    var inputField = this.variableList().get(item).element(by.model('variable.label'));

    if (arguments.length !== 0)
      inputField.sendKeys(inputValue);

    return inputField;
  },

  addVariable: function(name, label) {
 /*   var items;

    this.variableList().count()
        .then(function(countVar) {
          items = countVar;
          console.log(items);
        })
        .then(element(by.css('[ng-click="addVariable()"]')))
        .then(function(buttonElement) {
          buttonElement.click();
        });
*//*        .then(function() {
          this.variableNameInput(items, name);
          this.variableLabelInput(items, label)
        });*/

  }

});
