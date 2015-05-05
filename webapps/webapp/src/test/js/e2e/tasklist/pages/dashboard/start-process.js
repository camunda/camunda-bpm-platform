'use strict';

var Page = require('./dashboard-view');

module.exports = Page.extend({
  navigationLinkElement: function () {
    return element(by.css('.navbar li.start-process-action a'));
  },

  startProcessDialog: function () {
    return element(by.css('.modal .modal-content'));
  },


  businessKeyField: function () {
    return this.startProcessDialog().element(by.css('[cam-business-key]'));
  },


  genericFormAddVariableButton: function () {
    return element(by.css('[ng-click="addVariable()"]'));
  },

  genericFormRows: function () {
    var rows = this.startProcessDialog().all(by.css('form[name=generic-form] > .form-group[ng-form=repeatForm]'));
    if (arguments.length) { return rows.get(arguments[0]); }
    return rows;
  },

  genericFormRowsCount: function () {
    return this.genericFormRows().count();
  },

  genericFormRowRemoveButton: function(rowIndex) {
    return this.genericFormRows(rowIndex || 0)
      .element(by.css('[ng-click="removeVariable(delta)"]'));
  },

  genericFormRowNameField: function(rowIndex) {
    return this.genericFormRows(rowIndex || 0)
      .element(by.css('[ng-model="variable.name"]'));
  },

  genericFormRowTypeField: function(rowIndex) {
    return this.genericFormRows(rowIndex || 0)
      .element(by.css('[ng-model="variable.type"]'));
  },

  genericFormRowTypeFieldSelect: function (rowIndex, val) {
    var el = this.genericFormRows(rowIndex || 0);
    el.click().then(function () {
      el.element(by.css('[value="' + val + '"]')).click();
    });
  },

  genericFormRowValueField: function(rowIndex) {
    return this.genericFormRows(rowIndex || 0)
      .element(by.css('[ng-model="variable.value"]'));
  },

  startButton: function() {
    return this.startProcessDialog()
      .element(by.css('.modal-footer [ng-click="startProcessInstance()"]'));
  },

  backButton: function() {
    return this.startProcessDialog()
      .element(by.css('.modal-footer [ng-click="showList())"]'));
  },

  cancelButton: function() {
    return this.startProcessDialog()
      .element(by.css('.modal-footer [ng-click="close()"]'));
  },










  formElement: function() {
    return element(by.css('.modal-body form'));
  },

  searchProcessInput: function(inputValue) {
    var inputField = element(by.css('.modal-header input'));

    if (arguments.length !== 0)
      inputField.sendKeys(inputValue);

    return inputField;
  },

  foundProcesses: function() {
    return element.all(by.repeater('match in matches track by $index'));
  },

  selectProcessFromSearchResult: function(item) {
    return this.foundProcesses().get(item).element(by.binding('match.label')).click();
  },

  processList: function() {
    return element.all(by.repeater('process in processes'));
  },

  selectProcess: function(item) {
   return this.processList().get(item).element(by.binding('process.name')).click();
  },

  startProcessInstance: function(processName) {
    this.selectNavbarItem('Process');
    this.searchProcessInput().sendKeys(processName);
    this.selectProcessFromSearchResult(0);
    this.startButton().click();
  }
});
