'use strict';

var Page = require('./../dashboard-view');

module.exports = Page.extend({

  formElement: function() {
    return element(by.css('[cam-tasklist-filters]'));
  },

  filterList: function() {
    return this.formElement().all(by.repeater('(delta, filter) in filters'));
  },

  filterListInfoText: function() {
    return this.formElement().getText();
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
    var filter = this.filterList().get(item).element(by.css('[class="task-filter"]'));
    filter.click();
    return filter;
  },

  filterStatus: function(item) {
    return this.filterList().get(item).getAttribute('class');
  },

  isFilterSelected: function(item) {
    return this.filterStatus(item).then(function(matcher) {
      if (matcher.indexOf('active') !== -1) {
        return true;
      }
      return false;
    });
  },

  filterNameElement: function(item) {
    return this.filterList().get(item).element(by.binding('filter.name'));
  },

  filterName: function(item) {
    return this.filterNameElement(item).getText();
  },

  filterDescriptionElement: function(item) {
    browser.actions().mouseMove(this.filterNameElement(item)).perform();

    browser.sleep(1000);

    return this.filterList().get(item).element(by.css('.name')).getAttribute('tooltip');
  },

  filterDescription: function(item) {
    return this.filterDescriptionElement(item);
  },

  createFilterButton: function() {
    return element(by.css('[ng-click="openModal($event)"]'));
  },

  editFilter: function(item) {

    browser.sleep(1000);

    browser.actions().mouseMove(this.filterNameElement(item)).perform();

    browser.sleep(1000);

    var editFilterElement = this.filterList().get(item).element(by.css('[ng-click="openModal($event, filter)"]'));
    editFilterElement.click();
    return editFilterElement;
  }

});
