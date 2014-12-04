'use strict';

var Filter = require('./definition-view');

module.exports = Filter.extend({

  filterSideBarElement: function() {
    return element(by.css('.ctn-sidebar'));
  },

  addFilterButton: function() {
    return this.filterSideBarElement().element(by.css('.icon-plus'));
  },

  addFilterByVariable: function(filterValue) {
    this.addFilterButton().click().then(function() {
      element(by.css('[ng-click="addVariableFilter()"]')).click().then(function() {
        element(by.model('variable.value')).sendKeys(filterValue);
      });
    });
  },

  addFilterByBusinessKey: function() {

  },

  addFilterByStartDate: function() {

  }


});
