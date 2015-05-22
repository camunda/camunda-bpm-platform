'use strict';

var Page = require('./system-base');

module.exports = Page.extend({

  url: '/camunda/app/admin/default/#/system?section=system-settings-flow-node-count',

  resultField: function() {
    return element(by.binding('activityInstances')).getText();
  },

  startDateField: function(inputValue) {
    var inputField = element(by.model('startDate'));

    if (arguments.length !== 0)
      inputField.sendKeys(inputValue);

    return inputField;
  },

  endDateField: function(inputValue) {
    var inputField = element(by.model('endDate'));

    if (arguments.length !== 0)
      inputField.sendKeys(inputValue);

    return inputField;
  },

  refreshButton: function() {
    return element(by.css('[ng-click="load()"]'));
  }
});
