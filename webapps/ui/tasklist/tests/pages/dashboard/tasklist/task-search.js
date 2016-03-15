/* jshint node: true, unused: false */
/* global __dirname: false, describe: false, beforeEach: false, before:false, it: false, browser: false,
          element: false, expect: false, by: false, protractor: false */
'use strict';

var Page = require('./../dashboard-view');

module.exports = Page.extend({

  formElement: function() {
    return element(by.css('[cam-widget-search]'));
  },

  searchList: function() {
    return this.formElement().all(by.repeater('search in searches'));
  },

  variableTypeDropdown: function(type) {
    return this.formElement().element(by.cssContainingText('ul > li', type));
  },

  createSearch: function(type, name, operator, value) {
    this.formElement().element(by.css('.main-field')).click();
    this.variableTypeDropdown(type).click();
    if(name) {
      this.searchList().last().element(by.model('editValue')).sendKeys(name, protractor.Key.ENTER);
    }
    if(value) {
      this.searchList().last().element(by.model('editValue')).sendKeys(value, protractor.Key.ENTER);
    }
    if(operator) {
      this.searchList().last().element(by.css('[value="operator.value"]')).click();
      this.searchList().last().element(by.cssContainingText('[value="operator.value"] .dropdown-menu li', operator)).click();
    }
  },

  searchInputField: function() {
    return this.formElement().element(by.css('.main-field'));
  },

  deleteSearch: function(index) {
    this.searchList().get(index).element(by.css('.remove-search')).click();
  },

  changeType: function(index, type) {
    this.searchList().get(index).element(by.css('[cam-widget-inline-field][value="type.value"]')).click();
    this.searchList().get(index).element(by.cssContainingText('ul > li', type)).click();
  },

  changeOperator: function(index, operator) {
    this.searchList().get(index).element(by.css('[cam-widget-inline-field][value="operator.value"]')).click();
    this.searchList().get(index).element(by.cssContainingText('ul > li', operator)).click();
  },

  changeValue: function(index, value) {
    this.searchList().get(index).element(by.css('[cam-widget-inline-field][value="value.value"]')).click();

    var input = this.searchList().get(index).element(by.model('editValue'));
    return input.clear().then(function() {
      input.sendKeys(value, protractor.Key.ENTER);
    });
  }

});
