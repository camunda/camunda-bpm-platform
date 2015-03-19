'use strict';

var Filter = require('./definition-view');

module.exports = Filter.extend({

  filterSideBarElement: function() {
    return element(by.css('.ctn-sidebar'));
  },

  addFilterButton: function() {
    return this.filterSideBarElement().element(by.css('.glyphicon-plus'));
  },

  dropdownElement: function() {
    return this.filterSideBarElement().element(by.css('.dropdown-menu'));
  },

  variableFilters: function() {
    return this.filterSideBarElement().all(by.repeater('variable in filterData.variables'));
  },

  removeVariableFilter: function(item) {
    return this.variableFilters().get(item).element(by.css('.glyphicon-remove')).click();
  },

  removeBusinessKeyFilter: function() {
    return this.filterSideBarElement().element(by.css('[ng-click="removeBusinessKeyFilter()"]')).click();
  },

  addFilterByVariable: function(filterExpression) {
    var self = this;

    this.addFilterButton().click().then(function() {
      self.dropdownElement().element(by.css('[ng-click="addVariableFilter()"]')).click().then(function() {
        self.variableFilters().count().then(function(items) {
          items = items -1;
          self.variableFilters().get(items).element(by.model('variable.value')).sendKeys(filterExpression);
        });
      });
    });
  },

  addFilterByBusinessKey: function(filterExpression) {
    var self = this;

    this.addFilterButton().click().then(function() {
      self.dropdownElement().element(by.css('[ng-click="addBusinessKeyFilter()"]')).click().then(function() {
        self.filterSideBarElement().element(by.model('filterData.businessKey.value')).sendKeys(filterExpression);
      });
    });
  },

  addFilterByStartDate: function(direction, filterExpression) {
    var self = this;

    this.addFilterButton().click().then(function() {
      self.dropdownElement().element(by.css('[ng-click="addStartDateFilter()"]')).click().then(function() {
        self.filterSideBarElement().element(by.cssContainingText('option', direction)).click();
        if (filterExpression !== 0) {
          self.filterSideBarElement().element(by.model('date.value')).clear();
          self.filterSideBarElement().element(by.model('date.value')).sendKeys(filterExpression);
        }
      });
    });
  }

});
