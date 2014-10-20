'use strict';

var Page = require('./../dashboard-view');

module.exports = Page.extend({

  formElement: function() {
    return element(by.css('[cam-tasklist-filters]'));
  },

  filterList: function() {
    return this.formElement().all(by.repeater('(delta, filter) in filters'));
  },

  findFilter: function(filterName) {

    this.filterList().then(function(arr) {

      for (var i = 0; i < arr.length; ++i) {

        arr[i].getText().then(function (text) {

          console.log(text);
          if (filterName === text)
            console.log('got you ' + text);
        });
      }
    });
  },

  selectFilter: function(item) {
    var pile = this.filterList().get(item).element(by.css('[class="name ng-binding"]'));
    pile.click();
    return pile;
  },

  filterStatus: function(item) {
    return this.filterList().get(item).getAttribute('class');
  },

  isFilterSelected: function(item) {
    expect(this.filterStatus(item)).toMatch('active');
  },

  isFilterNotSelected: function(item) {
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
    return element(by.css('[ng-click="openDialog(\'CREATE_NEW_FILTER\')"]'));
  },

  editFilter: function(item) {

    browser.sleep(1000);

    protractor.getInstance().actions().mouseMove(this.filterNameElement(item)).perform();

    browser.sleep(1000);

    var editFilterElement = this.filterList().get(item).element(by.css('[ng-click="openDialog(\'EDIT_FILTER\')"]'));
    editFilterElement.click();
    return editFilterElement;
  },

  deleteFilter: function(item) {

    browser.sleep(1000);

    protractor.getInstance().actions().mouseMove(this.filterNameElement(item)).perform();

    browser.sleep(1000);

    var deleteFilterElement = this.filterList().get(item).element(by.css('[ng-click="openDialog(\'DELETE_FILTER\')"]'));
    deleteFilterElement.click();
    return deleteFilterElement;
  }

});