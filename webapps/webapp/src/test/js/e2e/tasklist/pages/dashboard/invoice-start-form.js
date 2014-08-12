'use strict';

var Page = require('./start-process');

module.exports = Page.extend({

  creditorInput: function() {
    return element(by.id('creditor'));
  },

  amountInput: function() {
    return element(by.id('amount'));
  },

  invoiceNumberInput: function() {
    return element(by.id('invoiceNumber'));
  },

  approverInput: function() {
    return element(by.name('approver'));
  }

});