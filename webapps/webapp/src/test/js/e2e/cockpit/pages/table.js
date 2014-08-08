'use strict';

var Table = require('./base');

module.exports = Table.extend({

  tableTabs: function() {
    return element.all(by.repeater(this.repeater));
  },

  selectTab: function() {
    this.tableTabs().get(this.tabIndex).click();
  },

  isTabSelected: function() {
    return this.tableTabs(this.repeater).get(this.tabIndex).getAttribute('class');
  },

  tabName: function() {
    return this.tableTabs(this.repeater).get(this.tabIndex).element(by.css('[class="ng-binding"]')).getText();
  }

});
