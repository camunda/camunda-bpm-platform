'use strict';

var Page = require('./dashboard-view');

module.exports = Page.extend({

  pileList: function() {
    return element.all(by.repeater('pile in piles'));
  },

  selectPile: function(item) {
    this. pileList().get(item).element(by.css('[class="name ng-binding"]')).click();
  }

});