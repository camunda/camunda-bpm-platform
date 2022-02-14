/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
        arr[i].getText().then(function(text) {
          console.log(text);
          if (filterName === text) console.log('got you ' + text);
        });
      }
    });
  },

  selectFilter: function(item) {
    return this.filterList()
      .get(item)
      .click();
  },

  filterStatus: function(item) {
    return this.filterList()
      .get(item)
      .getAttribute('class');
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
    return this.filterList()
      .get(item)
      .element(by.binding('filter.name'));
  },

  filterName: function(item) {
    return this.filterNameElement(item).getText();
  },

  filterDescriptionElement: function(item) {
    browser
      .actions()
      .mouseMove(this.filterNameElement(item))
      .perform();

    browser.sleep(1000);

    return this.filterList()
      .get(item)
      .element(by.css('.name'))
      .getAttribute('tooltip');
  },

  filterDescription: function(item) {
    return this.filterDescriptionElement(item);
  },

  createFilterButton: function() {
    return element(by.css('[ng-click="openModal($event)"]'));
  },

  createFilter: function() {
    var theElement = element(by.css('.modal-title'));
    this.createFilterButton().click();
    this.waitForElementToBeVisible(theElement, 5000);
  },

  editFilter: function(item) {
    var self = this;
    this.selectFilter(item)
      .element(by.css('[ng-click="openModal($event, filter)"]'))
      .click();
    // browser.actions().mouseMove(this.filterNameElement(item)).perform().then(function() {
    //   self.filterList().get(item).element(by.css('[ng-click="openModal($event, filter)"]')).click();
    // });
  }
});
