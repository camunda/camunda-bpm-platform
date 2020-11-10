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
    return element(by.css('[cam-sorting-choices]'));
  },

  sortingList: function() {
    return this.formElement().all(by.repeater('(index, sorting) in sortings'));
  },

  sortingName: function(index) {
    return this.sortingList()
      .get(index)
      .element(by.css('.sort-by'));
  },

  changeSortingDirection: function(index) {
    return this.sortingList()
      .get(index)
      .element(by.css('[ng-click="changeOrder(index)"]'))
      .click();
  },

  sortingDirection: function(index) {
    return this.sortingList()
      .get(index)
      .element(by.css('.sort-direction'))
      .getAttribute('class');
  },

  isSortingDescending: function(index) {
    return this.sortingDirection(index).then(function(matcher) {
      if (matcher.indexOf('-down') !== -1) {
        return true;
      }
      return false;
    });
  },

  isSortingAscending: function(index) {
    return this.sortingDirection(index).then(function(matcher) {
      if (matcher.indexOf('-up') !== -1) {
        return true;
      }
      return false;
    });
  },

  addSortingButton: function() {
    return this.formElement().element(by.css('.dropdown.new-sort'));
  },

  removeSortingButton: function(index) {
    return this.sortingList()
      .get(index)
      .element(by.css('[ng-click="removeSorting(index)"]'));
  },

  sortingSelectionListElement: function(index, sortingType) {
    return this.sortingList()
      .get(index)
      .element(by.css('.sorting-choice .dropdown-menu'))
      .element(by.cssContainingText('a.ng-scope', sortingType));
  },

  newSortingSelectionListElement: function(sortingType) {
    return this.formElement()
      .element(by.css('.dropdown.new-sort'))
      .element(by.cssContainingText('a.ng-scope', sortingType));
  },

  /**
   * Add new sortin
   *
   * @param  {String} [sorting type]    'Assingee'
   *                                    'Created'
   *                                    'Due Date'
   *                                    'Follow-up date'
   *                                    'Task name'
   *                                    'Priority'
   *                                    'Case Execution Variable'
   *                                    'Case Instance Variable'
   *                                    'Execution Variable'
   *                                    'Process Variable'
   *                                    'Task Variable'
   */
  addNewSorting: function(sortingType, variableName, variableType) {
    var varInput = false;
    if (arguments.length >= 2) {
      varInput = true;
    }
    this.addSortingButton().click();
    var listElement = this.newSortingSelectionListElement(sortingType);
    listElement.click().then(function() {
      if (varInput) {
        listElement
          .element(by.xpath('..'))
          .element(by.model('variable.varName'))
          .sendKeys(variableName);
        listElement
          .element(by.xpath('..'))
          .element(by.cssContainingText('.ng-scope', variableType))
          .click();
        listElement
          .element(by.xpath('..'))
          .element(by.css('[ng-click="applySorting($event)"]'))
          .click();
      }
    });
  },

  changeSorting: function(index, sortingType, variableName, variableType) {
    var varInput = false;
    if (arguments.length >= 3) {
      varInput = true;
    }
    this.sortingName(index).click();
    this.sortingSelectionListElement(index, sortingType)
      .click()
      .then(function() {
        if (varInput) {
          element(by.css('.variable-inputs'))
            .element(by.model('variable.varName'))
            .sendKeys(variableName);
          element(by.css('.variable-inputs'))
            .element(by.cssContainingText('.ng-scope', variableType))
            .click();
          element(by.css('.variable-inputs'))
            .element(by.css('[ng-click="applySorting($event)"]'))
            .click();
        }
      });
  }
});
