'use strict';

var Page = require('./repository-view');

module.exports = Page.extend({

  formElement: function() {
    return element(by.css('.deployments'));
  },


  // deployments ///////////////////////////////////////////

  deploymentList: function() {
    return this.formElement().all(by.repeater('(delta, deployment) in deployments'));
  },

  deploymentName: function(index) {
    return this.deploymentList().get(index).element(by.css('.name')).getText();
  },

  // sorting //////////////////////////////////////////////

  sortingElement: function() {
    return this.formElement().element(by.css('[cam-deployments-sorting-choices]'));
  },

  sortingBy: function() {
    return this.sortingElement().element(by.css('.sort-by')).getText();
  },

  changeSortingBy: function(sortBy) {
    var self = this;
    this.sortingElement().element(by.css('.dropdown')).click().then(function() {
      self.sortingElement().element(by.cssContainingText('.sort-by-choice', sortBy)).click();
    });
  },

  changeSortingDirection: function() {
    this.sortingElement().element(by.css('[ng-click="changeOrder()"]')).click();
  },

  sortingDirection: function() {
    return this.sortingElement().element(by.css('.sort-direction')).getAttribute('class');
  },

  isSortingDescending: function() {
    return this.sortingDirection().then(function(matcher) {
      if (matcher.indexOf('-down') !== -1) {
        return true;
      }
      return false;
    });
   },

   isSortingAscending: function() {
    return this.sortingDirection().then(function(matcher) {
      if (matcher.indexOf('-up') !== -1) {
        return true;
      }
      return false;
    });
   },


   // search ////////////////////////////////////////////////

  searchElement: function() {
    return this.formElement().element(by.css('[cam-widget-search]'));
  },

  searchList: function() {
    return this.searchElement().all(by.repeater('search in searches'));
  },

  searchInputField: function() {
    return this.searchElement().element(by.css('.main-field'));
  },

  searchTypeDropdown: function(type) {
    return this.searchElement().element(by.cssContainingText('ul > li', type));
  },

  createSearch: function(type, operator, value, isDateValue) {
    this.searchElement().element(by.css('.main-field')).click();
    this.searchTypeDropdown(type).click();

    if(value) {
      if (isDateValue) {
        element(by.css('.cam-widget-inline-field > button[ng-click="changeType()"]')).click();
      }
      this.searchList().last().element(by.model('editValue')).sendKeys(value, protractor.Key.ENTER);
    }

    if (operator) {
      this.searchList().last().element(by.css('[value="operator.value"]')).click();
      this.searchList().last().element(by.cssContainingText('[value="operator.value"] .dropdown-menu li', operator)).click();
    }
  },

  deleteSearch: function(index) {
    this.searchList().get(index).element(by.css('.remove-search')).click();
  },

  changeType: function(index, type) {
    this.searchList().get(index).element(by.css('[cam-widget-inline-field][value="type.value"]')).click();
    this.searchList().get(index).element(by.cssContainingText('ul > li', type)).click();
  },

  changeOperator: function(index, operator) {
    this.searchList().get(index).element(by.css('[cam-widget-inline-field][value="operator.value"]')).click();
    this.searchList().get(index).element(by.cssContainingText('ul > li', operator)).click();
  },

  changeValue: function(index, value, isDateValue) {
    this.searchList().get(index).element(by.css('[cam-widget-inline-field][value="value.value"]')).click();

    if (isDateValue) {
      if (isDateValue) {
        element(by.css('.cam-widget-inline-field > button[ng-click="changeType()"]')).click();
      }
    }

    this.searchList().get(index).element(by.model('editValue')).sendKeys(value, protractor.Key.ENTER);
  }


});
