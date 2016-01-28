'use strict';

var Page = require('./../start-process-modal');

module.exports = Page.extend({

  creditorInput: function(inputValue) {
    var inputField = element(by.id('creditor'));

    if (arguments.length !== 0)
      inputField.sendKeys(inputValue);

    return inputField;
  },

  amountInput: function(inputValue) {
    var inputField = element(by.id('amount'));

    if (arguments.length !== 0)
      inputField.sendKeys(inputValue);

    return inputField;
  },

  invoiceNumberInput: function(inputValue) {
    var inputField = element(by.id('invoiceNumber'));

    if (arguments.length !== 0)
      inputField.sendKeys(inputValue);

    return inputField;
  },

  approverInput: function(inputValue) {
    var inputField = element(by.name('approver'));

    if (arguments.length !== 0)
      inputField.sendKeys(inputValue);

    return inputField;
  }

});
