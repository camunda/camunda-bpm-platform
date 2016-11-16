'use strict';

var Page = require('../repository-view');

module.exports = Page.extend({

  formElement: function() {
    return element(by.css('.drd-definitions.cam-table tbody'));
  },

  name: function(idx) {
    return this.formElement().element(by.css('.name'));
  },

  key: function(idx) {
    return this.formElement().element(by.css('.key'));
  },

  version: function(idx) {
    return this.formElement().element(by.css('.version'));
  },

});
