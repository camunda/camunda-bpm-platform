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

var Base = require('./../../../cockpit/tests/pages/base');

module.exports = Base.extend({
  formElement: function() {
    return element(by.css('[cam-widget-search]'));
  },

  createSearch: function(type, operator, value, name) {
    if (!value) {
      value = operator;
      operator = undefined;
    }

    // create search
    var el = this.formElement();

    el.element(by.css('.main-field')).click();

    el.element(by.cssContainingText('ul > li', type)).click();

    if (name) {
      this.searchPills()
        .last()
        .element(by.model('editValue'))
        .sendKeys(name, protractor.Key.ENTER);
    }

    // add value to search
    if (value) {
      this.searchPills()
        .last()
        .element(by.model('editValue'))
        .sendKeys(value, protractor.Key.ENTER);
    }

    // change operator if necessary
    if (operator) {
      this.searchPills()
        .last()
        .element(by.css('[value="operator.value"]'))
        .click();

      this.searchPills()
        .last()
        .element(
          by.cssContainingText(
            '[value="operator.value"] .dropdown-menu li',
            operator
          )
        )
        .click();
    }
  },

  deleteSearch: function(idx) {
    this.searchPills()
      .get(idx)
      .element(by.css('.remove-search'))
      .click();
  },

  clearSearch: function() {
    this.searchPills()
      .all(by.css('.remove-search'))
      .click();
  },

  searchPills: function() {
    return this.formElement().all(by.css('[cam-widget-search-pill]'));
  }
});
