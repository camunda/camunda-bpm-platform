'use strict';

var Page = require('./dashboard-view');

module.exports = Page.extend({

  startProcessDialog: function () {
    return element(by.css('.modal .modal-content'));
  },

  openStartDialog: function() {
    var elementToWaitFor = this.searchProcessInput();

    this.selectNavbarItem('Process');
    this.waitForElementToBeVisible(elementToWaitFor, 5000);
  },

  openStartDialogAndSelectProcess: function(processName) {
    this.openStartDialog();

    if (arguments.length === 1 && typeof processName === 'number') {
      this.selectProcessByIndex(processName);
    } else {
      this.selectProcessByName(processName);
    }
  },

  backButton: function() {
    return this.startProcessDialog()
      .element(by.css('[ng-click="back()"]'));
  },

  closeButton: function() {
    return this.startProcessDialog()
      .element(by.css('[ng-click="$dismiss()"]'));
  },

  closeStartDialog: function() {
    var closeButtonElement = this.closeButton();
    closeButtonElement.click();
    this.waitForElementToBeNotPresent(closeButtonElement, 5000);
  },

  startButton: function() {
    return this.startProcessDialog()
      .element(by.css('[ng-click="startProcessInstance()"]'));
  },

  startProcess: function() {
    var startButtonElement = this.startButton();
    startButtonElement.click();
    this.waitForElementToBeNotPresent(startButtonElement, 5000);
  },

  searchProcessInput: function(inputValue) {
    var inputField = this.startProcessDialog().
                      element(by.css('.modal-header input'));

    if (arguments.length !== 0)
      inputField.sendKeys(inputValue);

    return inputField;
  },

  processList: function() {
    return this.startProcessDialog()
      .all(by.repeater('processDefinition in processDefinitions'));
  },

  selectProcessByIndex: function(idx) {
    var clickElement = this.processList().get(idx)
                        .element(by.css('[ng-click="selectProcessDefinition(processDefinition)"]'));
    this.waitForElementToBeVisible(clickElement, 8000);

    var elementToWaitFor = element(by.css('[ng-click="addVariable()"]'));
    clickElement.click();

    this.waitForElementToBeVisible(elementToWaitFor, 8000);
  },

  selectProcessByName: function(name) {
    var that = this;

    this.findElementIndexInRepeater('processDefinition in processDefinitions', by.css('[class="ng-binding"]'), name)
        .then(function(idx) {
          that.selectProcessByIndex(idx);
        });
  },

  processListInfoText: function() {
    return this.startProcessDialog()
      .element(by.css('.glyphicon-info-sign'));
  },
  
  processTenantIdField: function(idx) {
    return this.processList().get(idx).element(by.css('.tenant-id'));
  }

});
