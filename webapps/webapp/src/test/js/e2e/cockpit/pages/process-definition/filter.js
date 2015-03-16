'use strict';

var Filter = require('./definition-view');

module.exports = Filter.extend({

  filterSideBarElement: function() {
    return element(by.css('.ctn-sidebar'));
  },

  activityFilter: function(activityName) {
    return this.filterSideBarElement().element(by.cssContainingText('.activity-filter >.search', activityName));
  },

  removeFilterButton: function(activityName) {
    return this.activityFilter(activityName).element(by.css('button'));
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
