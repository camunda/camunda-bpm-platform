'use strict';

var Page = require('./filter-modal');

module.exports = Page.extend({

  closeButton: function() {
    return this.formElement().element(by.css('[ng-click="$dismiss()"]'));
  },

  deleteButton: function() {
    return this.formElement().element(by.css('[ng-click="delete()"]'));
  },

  editFilterButton: function() {
    return this.formElement().element(by.css('[ng-click="abortDeletion()"]'));
  }

});