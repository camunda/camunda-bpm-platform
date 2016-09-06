'use strict';

var Base = require('./../../../cockpit/tests/pages/base');

module.exports = Base.extend({
  formElement: function() {
    return element(by.css('[cam-widget-search]'));
  },

  createSearch: function(type, operator, value, name) {
    if(!value) {
      value = operator;
      operator = undefined;
    }

    // create search
    var el = this.formElement();

    el.element(by.css('.main-field'))
      .click();

    el.element(by.cssContainingText('ul > li', type))
      .click();

    if (name) {
      this
        .searchPills().last()
        .element(by.model('editValue'))
        .sendKeys(name, protractor.Key.ENTER);
    }

    // add value to search
    if(value) {
      this
        .searchPills()
        .last()
        .element(by.model('editValue'))
        .sendKeys(value, protractor.Key.ENTER);
    }

    // change operator if necessary
    if(operator) {
      this
        .searchPills()
        .last()
        .element(by.css('[value="operator.value"]'))
        .click();

      this
        .searchPills()
        .last()
        .element(by.cssContainingText('[value="operator.value"] .dropdown-menu li', operator))
        .click();
    }
  },

  deleteSearch: function(idx) {
    this
      .searchPills()
      .get(idx)
      .element(by.css('.remove-search'))
      .click();
  },

  clearSearch: function() {
    this
      .searchPills()
      .all(by.css('.remove-search'))
      .click();
  },

  searchPills: function() {
    return this
      .formElement()
      .all(by.css('[cam-widget-search-pill]'));
  }
});
