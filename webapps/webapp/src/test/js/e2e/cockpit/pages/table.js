'use strict';

var Base = require('./base');

module.exports = Base.extend({

  tableTabs: function(repeater) {
    return element.all(by.repeater(repeater));
  },

  selectTab: function(repeater, tab) {
    this.tableTabs(repeater).get(tab).click();
  },

  isTabSelected: function(repeater, tab) {
    return this.tableTabs(repeater).get(tab).getAttribute('class');
  },

  tabName: function(repeater, tab) {
    return this.tableTabs(repeater).get(tab).element(by.css('[class="ng-binding"]')).getText();
  }

});
