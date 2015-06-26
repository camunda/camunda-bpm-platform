'use strict';

var Page = require('./filter-modal');

module.exports = Page.extend({

  saveButton: function() {
    return this.formElement().element(by.css('[ng-click="submit()"]'));
  },

  saveFilter: function() {
    var theElement = this.formElement();
    this.saveButton().click();
    this.waitForElementToBeNotPresent(theElement, 5000);
  },

  closeButton: function() {
    return this.formElement().element(by.css('[ng-click="$dismiss()"]'));
  },

  closeFilter: function() {
    var theElement = this.formElement();
    this.closeButton().click();
    this.waitForElementToBeNotPresent(theElement, 5000);
  },

  deleteFilterButton: function() {
    return this.formElement().element(by.css('[ng-click="confirmDeletion()"]'));
  }

});
