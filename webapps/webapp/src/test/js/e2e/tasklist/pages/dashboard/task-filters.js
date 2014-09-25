'use strict';

var Page = require('./dashboard-view');

module.exports = Page.extend({

  formElement: function () {
    return element(by.css('[cam-tasklist-filters]'));
  },

  filterList: function () {
    return this.formElement().all(by.repeater('(delta, filter) in filters'));
  },

  selectFilter: function (item) {
    var pile = this.filterList().get(item).element(by.css('[class="name ng-binding"]'));
    pile.click();
    return pile;
  },

  filterStatus: function (item) {
    return this.filterList().get(item).getAttribute('class');
  },

  isFilterSelected: function (item) {
    expect(this.filterStatus(item)).toMatch('active');
  },

  isFilterNotSelected: function (item) {
    expect(this.filterStatus(item)).not.toMatch('active');
  },

  filterNameElement: function(item) {
    return this.filterList().get(item).element(by.binding('filter.name'));
  },

  filterName: function(item) {
    return this.filterNameElement(item).getText();
  },

  filterDescriptionElement: function(item) {
    protractor.getInstance().actions().mouseMove(this.filterNameElement(item)).perform();

    browser.sleep(1000);

    return this.filterList().get(item).element(by.binding('filter.properties.description'));
  },

  filterDescription: function(item) {
    return this.filterDescriptionElement(item).getText();
  },

  createFilterButton: function() {
    return element(by.css('[ng-click="createFilter()"]'));
  },

  editFilter: function(item) {
    protractor.getInstance().actions().mouseMove(this.filterNameElement(item)).perform();

    browser.sleep(1000);

    var editFilterElement = this.filterList().get(item).element(by.css('[ng-click="edit(filter)"]'));
    editFilterElement.click();
    return editFilterElement;
  },

  deleteFilter: function(item) {
    protractor.getInstance().actions().mouseMove(this.filterNameElement(item)).perform();

    browser.sleep(1000);

    var deleteFilterElement = this.filterList().get(item).element(by.css('[ng-click="delete(filter)"]'));
    deleteFilterElement.click();
    return deleteFilterElement;
  }

});