'use strict';

var Table = require('./base');

module.exports = Table.extend({

  tableTabs: function() {
    return element.all(by.repeater(this.tabRepeater));
  },

  selectTab: function() {
    return this.tableTabs().get(this.tabIndex).click();
  },

  tabSelectionStatus: function() {
    return this.tableTabs(this.tabRepeater).get(this.tabIndex).getAttribute('class');
  },

  isTabSelected: function() {
    expect(this.tabSelectionStatus()).toMatch('ng-scope active');
  },

  isTabNotSelected: function() {
    expect(this.tabSelectionStatus()).not.toMatch('ng-scope active');
  },

  tabName: function() {
    return this.tableTabs(this.repeater).get(this.tabIndex).element(by.css('[class="ng-binding"]')).getText();
  },

  checkTabName: function() {
    expect(this.tabName()).toBe(this.tabLabel);
  },

  table: function() {
    return element.all(by.repeater(this.tableRepeater));
  },

  tableItem: function(item, bindingSelector) {
    return this.table().get(item).element(by.binding(bindingSelector));
  }

});
