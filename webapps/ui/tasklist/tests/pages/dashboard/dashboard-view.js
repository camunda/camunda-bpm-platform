'use strict';

var Base = require('./../base');

module.exports = Base.extend({

  url: '/camunda/app/tasklist/default/#/',

  header: function () {
    return element(by.css('[cam-widget-header]'));
  },

  accountDropdown: function () {
    return this.header().element(by.css('.account.dropdown'));
  },

  accountDropdownButton: function () {
    return this.accountDropdown().element(by.css('.dropdown-toggle'));
  }
});
