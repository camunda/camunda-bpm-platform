'use strict';

var Base = require('./../base');

module.exports = Base.extend({

  getVersion: function() {
    this.waitForElementToBeVisible(element(by.css('.version-filter > .ng-scope')));
    return element(by.css('.version-filter')).getText().then(function(text) {
      return text.replace('Version\n', '');
    });
  },

  getDropdownButton: function() {
    this.waitForElementToBeVisible(element(by.css('.version-filter button')));
    return element(by.css('.version-filter button'));
  },

  getDropdownOptions: function() {
    return element.all(by.css('.version-filter li'));
  },

  getDropdownOption: function(idx) {
    return element.all(by.css('.version-filter li')).get(idx);
  }


});
