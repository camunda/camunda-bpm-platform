'use strict';

var Page = require('./../base');

module.exports = Page.extend({

  url: '/camunda/app/cockpit/default/#/reports',

  reportsType: function() {
    return element(by.css('[cam-reports-type]'));
  },

  noReportsAvailableHint: function() {
    return this.reportsType().element(by.css('.no-reports-available'));
  },

  getReportsTypeSelection: function() {
    return this.reportsType().element(by.css('[ng-model="selection.type"]'));
  },

  getReportsTypeOption: function(type) {
    return this.getReportsTypeSelection().element(by.cssContainingText('option', type));
  },

  getSelectedReportsTypeOption: function() {
    return this.getReportsTypeSelection().element(by.css('[selected="selected"]'));
  }

});
