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
    return this.tabSelectionStatus().then(function(classes) {
      return classes.indexOf('active') !== -1;
    });
  },

  tabName: function() {
    return this.tableTabs(this.repeater).get(this.tabIndex).element(by.css('[class="ng-binding"]')).getText();
  },

  table: function() {
    return element(by.css('.ctn-tabbed-content')).all(by.repeater(this.tableRepeater));
  },

  tableItem: function(item, elementSelector) {
    if (arguments.length === 1) {
      return this.table().get(item);
    }

    if (typeof elementSelector === 'string') {
      elementSelector = by.css(elementSelector);
    }

    return this.table().get(item).element(elementSelector);
  }

});
