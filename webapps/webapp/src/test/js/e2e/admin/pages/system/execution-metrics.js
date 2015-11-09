'use strict';

var Page = require('./system-base');

module.exports = Page.extend({

  url: '/camunda/app/admin/default/#/system?section=system-settings-metrics',

  flowNodesResult: function() {
    return element(by.binding('metrics.flowNodes')).getText();
  },

  decisionElementsResult: function() {
    return element(by.binding('metrics.decisionElements')).getText();
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
