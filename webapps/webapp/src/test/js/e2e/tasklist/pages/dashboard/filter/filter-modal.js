'use strict';

var Base = require('./../../base');

module.exports = Base.extend({

  formElement: function() {
    return element(by.css('.modal-content'));
  },

  formHeader: function() {
    return this.formElement().element(by.css('.modal-title')).getText();
  },

  selectPanelByKey: function (key) {
    var selecta = 'accordion [is-open="accordion.' + key + '"]';
    var btnSelecta = selecta + ' [ng-click="toggleOpen()"]';
    this.formElement().element(by.css(btnSelecta)).click();
    return this.isPanelOpen(key);
  },

  isPanelOpen: function(key) {
    var selecta = 'accordion [is-open="accordion.' + key + '"]';
    var bdySelecta = selecta + ' .panel-body';
    return element(by.css(bdySelecta)).isDisplayed();
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

  removeCriterionButton: function(idx) {
    return this.criterionList().get(idx).element(by.css('[ng-click="removeCriterion(delta)"]'));
  },

  criterionList: function() {
    return this.formElement().all(by.repeater('(delta, queryParam) in query'));
  },

  selectCriterionKey: function(item, group, key) {
    this.criterionList().get(item).element(by.cssContainingText('optgroup[label="' + group + '"] > option', key)).click();
  },

  criterionKeyInput: function(idx, inputKey) {
    var inputField = this.criterionList().get(idx).element(by.model('queryParam.key'));

    if (arguments.length !== 0)
      inputField.sendKeys(inputKey);

    return inputField;
  },

  criterionValueInput: function(idx, inputValue) {
    var inputField = this.criterionList().get(idx).element(by.model('queryParam.value'));

    if (arguments.length !== 0)
      inputField.sendKeys(inputValue);

    return inputField;
  },

  includeAssignedTasksCheckbox: function () {
    return this.formElement().element(by.css('[ng-model="filter.includeAssignedTasks"]'));
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

  editCriteria: function(idx, group, key, value) {
    this.selectCriterionKey(idx, group, key);
    this.criterionValueInput(idx).clear();
    this.criterionValueInput(idx, value);
  },

  // variables
  showUndefinedVariablesCheckBox: function() {
    return element(by.css('[cam-tasklist-filter-modal-form-variable]'))
                            .element(by.model('filter.properties.showUndefinedVariable'));
  },

  addVariableButton: function() {
    return this.formElement().element(by.css('[ng-click="addVariable()"]'));
  },

  removeVariableButton: function(idx) {
    return this.variableList().get(idx).element(by.css('[ng-click="removeVariable(delta)"]'));
  },

  variableList: function() {
    return this.formElement().all(by.repeater('(delta, variable) in variables'));
  },

  variableNameInput: function(idx, inputValue) {
    var inputField = this.variableList().get(idx).element(by.model('variable.name'));

    if (arguments.length !== 0)
      inputField.sendKeys(inputValue);

    return inputField;
  },

  variableLabelInput: function(idx, inputValue) {
    var inputField = this.variableList().get(idx).element(by.model('variable.label'));

    if (arguments.length !== 0)
      inputField.sendKeys(inputValue);

    return inputField;
  },

  addVariable: function(name, label) {
    var self = this;

    this.addVariableButton().click().then(function() {
      self.variableList().count().then(function(items) {
        items = items -1;
        self.variableNameInput(items, name);
        self.variableLabelInput(items, label);
      });
    });
  }

});
