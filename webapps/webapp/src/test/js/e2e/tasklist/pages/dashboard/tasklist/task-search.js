/* jshint node: true, unused: false */
/* global __dirname: false, describe: false, beforeEach: false, before:false, it: false, browser: false,
          element: false, expect: false, by: false, protractor: false */
'use strict';

var Page = require('./../dashboard-view');

module.exports = Page.extend({

  formElement: function() {
    return element(by.css('[cam-widget-search]'));
  },

  createSearch: function(type, name, operator, value) {
    this.formElement().click();
    this.formElement().element(by.cssContainingText('ul > li', type)).click();
    this.formElement().element(by.css('input')).sendKeys(name, protractor.Key.ENTER);
    this.formElement().element(by.css('[cam-widget-inline-field][value="operator.value"]')).click();
    this.formElement().element(by.cssContainingText('ul > li', operator)).click();
    this.formElement().element(by.css('[cam-widget-inline-field][value="value.value"]')).click();
    this.formElement().element(by.css('input')).sendKeys(value, protractor.Key.ENTER);
  },

  deleteSearch: function(idx) {
    this.formElement().all(by.css('[cam-widget-search-pill]')).get(idx).element(by.css('.remove-search')).click();
  }

});
