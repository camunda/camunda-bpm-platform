'use strict';

var Page = require('./dashboard-view');

module.exports = Page.extend({

  formElement: function() {
    return element(by.css('[cam-tasklist-filters]'));
  },

  filterList: function() {
    return this.formElement().all(by.repeater('(delta, filter) in filters'));
  },

  selectFilter: function(item) {
    var pile = this.filterList().get(item).element(by.css('[class="name ng-binding"]'));
    pile.click();
    return pile;
  }

});