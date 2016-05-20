'use strict';

var Page = require('./dashboard-view');

module.exports = Page.extend({

  createTaskDialog: function () {
    return element(by.css('.modal .modal-content'));
  },

  openCreateDialog: function() {
    var elementToWaitFor = this.searchProcessInput();

    this.selectNavbarItem('Task');
    this.waitForElementToBeVisible(elementToWaitFor, 5000);
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

  taskTenantIdField: function(idx) {
    return this.createTaskDialog().get(idx).element(by.css('.tenant-id'));
  }

});
