'use strict';

var Base = require('./../../base');

module.exports = Base.extend({

  formElement: function() {
    return element(by.css('.modal-content'));
  },

  formHeader: function() {
    return this.formElement().element(by.css('.modal-title')).getText();
  },

  selectPanel: function(panelItem) {
    var index = [
      'General',
      'Criteria',
      'Permission',
      'Variables'
    ];
    var item;
    var itemIndex = index.indexOf(panelItem) + 1;

    if (itemIndex)
      item = this.formElement().element(by.css('accordion .panel:nth-child(' + itemIndex + ') [ng-click="toggleOpen()"]'));
    else
      item = this.formElement().element(by.css('accordion .panel:nth-child(1) [ng-click="toggleOpen()"]'));

    return item.click();
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
  authorizationnList: function() {
    return this.formElement().all(by.repeater('(delta, authorization) in authorizations'));
  },

  // criteria
  addCriterionButton: function() {
    return this.formElement().element(by.css('[ng-click="addCriterion()"]'));
  },

  removeCriterionButton: function(item) {
    return this.criterionList().get(item).element(by.css('[ng-click="removeCriterion(delta)"]'));
  },

  criterionList: function() {
    return this.formElement().all(by.repeater('(delta, queryParam) in query'));
  },

  selectCriterionKey: function(item, group, key) {
    this.criterionList().get(item).element(by.cssContainingText('optgroup[label="' + group + '"] > option', key)).click();
  },

  criterionKeyInput: function(item, inputKey) {
    var inputField = this.criterionList().get(item).element(by.model('queryParam.key'));

    if (arguments.length !== 0)
      inputField.sendKeys(inputKey);

    return inputField;
  },

  criterionValueInput: function(item, inputValue) {
    var inputField = this.criterionList().get(item).element(by.model('queryParam.value'));

    if (arguments.length !== 0)
      inputField.sendKeys(inputValue);

    return inputField;
  },

  addCriteria: function(group, key, value) {
    var self = this;

    this.addCriterionButton().click().then(function() {
      self.criterionList().count().then(function(items) {
        items = items -1;
        self.selectCriterionKey(items, group, key);
        self.criterionValueInput(items, value);
      });
    });
  },

  // variables
  showUndefinedVariablesCheckBox: function() {
    return element(by.css('[cam-tasklist-filter-modal-form-variable]'))
                            .element(by.model('filter.properties.showUndefinedVariable'));
  },

  addVariableButton: function() {
    return this.formElement().element(by.css('[ng-click="addVariable()"]'));
  },

  removeVariableButton: function(item) {
    return this.variableList().get(item).element(by.css('[ng-click="removeVariable(delta)"]'));
  },

  variableList: function() {
    return this.formElement().all(by.repeater('(delta, variable) in variables'));
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
    var self = this;

    this.addVariableButton().then(function(button) {
      button.click();
      self.variableList().count().then(function(items) {
        items = items -1;
        self.variableNameInput(items, name);
        self.variableLabelInput(items, label);
      });
    });
  }

});
