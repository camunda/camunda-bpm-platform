'use strict';

var Page = require('./dashboard-view');

module.exports = Page.extend({

  createTaskDialog: function () {
    return element(by.css('.modal .modal-content'));
  },

  openCreateDialog: function() {
    this.selectNavbarItem('Task');
    var taskNameFieldElement = element(by.css('.modal-content'));
    return this.waitForElementToBeVisible(taskNameFieldElement, 5000);
  },

  closeButton: function() {
    return this.createTaskDialog()
      .element(by.css('[ng-click="$dismiss()"]'));
  },

  closeCreateDialog: function() {
    var closeButtonElement = this.closeButton();
    closeButtonElement.click();
    this.waitForElementToBeNotPresent(closeButtonElement, 5000);
  },

  saveButton: function() {
    return this.createTaskDialog()
      .element(by.css('[ng-click="save()"]'));
  },

  saveTask: function() {
    var saveButtonElement = this.saveButton();
    saveButtonElement.click();
    this.waitForElementToBeNotPresent(saveButtonElement, 5000);
  },

  taskNameField: function() {
      return this.createTaskDialog().element(by.css('input[name="taskName"]'));
  },

  taskAssigneeField: function() {
      return this.createTaskDialog().element(by.css('input[name="taskAssignee"]'));
  },

  taskTenantIdField: function() {
    return this.createTaskDialog().element(by.css('select[name="taskTenantId"]'));
  },

  taskNameInput: function(inputValue) {
      var inputField = this.taskNameField();

      if (arguments.length !== 0)
        inputField.sendKeys(inputValue);

      return inputField;
  },

  taskAssigneeInput: function(inputValue) {
      var inputField = this.taskAssigneeField();

      if (arguments.length !== 0)
        inputField.sendKeys(inputValue);

      return inputField;
  }
});
