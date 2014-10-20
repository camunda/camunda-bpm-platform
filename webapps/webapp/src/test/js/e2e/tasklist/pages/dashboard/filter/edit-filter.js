'use strict';

var Page = require('./filter-modal');

module.exports = Page.extend({

  saveButton: function() {
    return this.formElement().element(by.css('[ng-click="submit()"]'));
  },

  closeButton: function() {
    return this.formElement().element(by.css('[ng-click="$dismiss()"]'));
  },

  deleteFilterButton: function() {
    return this.formElement().element(by.css('[ng-click="confirmDeletion()"]'));
  }

});