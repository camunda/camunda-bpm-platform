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

var Base = require('./../base');

module.exports = Base.extend({
  formElement: function() {
    return element(by.css('.filters'));
  },

  instanceFilterInput: function(inputValue) {
    var inputField = this.formElement().element(by.model('name'));

    if (arguments.length !== 0) inputField.sendKeys(inputValue);

    return inputField;
  },

  instanceSelectionLabel: function() {
    return this.formElement()
      .all(by.css('ng-pluralize'))
      .get(0);
  },

  instanceSelectionRemoveIcon: function() {
    return this.formElement().element(by.css('[ng-click="clearSelection()"]'));
  },

  clearInstanceSelection: function() {
    this.instanceSelectionRemoveIcon().click();
  },

  selectInstance: function(activityName) {
    this.formElement()
      .element(by.cssContainingText('.tree-node-label', activityName))
      .click();
  },

  deselectInstance: function(activityName) {
    this.formElement()
      .element(by.cssContainingText('.tree-node-label', activityName))
      .element(by.css('[ng-click="deselect($event)"]'))
      .click();
  },

  isInstanceSelected: function(activityName) {
    return this.formElement()
      .element(by.cssContainingText('.tree-node-label', activityName))
      .getAttribute('class')
      .then(function(classes) {
        return classes.indexOf('selected') !== -1;
      });
  }
});
