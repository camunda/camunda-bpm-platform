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

var Page = require('./../start-process-modal');

module.exports = Page.extend({
  genericFormElement: function() {
    return element(by.css('.generic-form-fields'));
  },

  helpText: function() {
    return this.genericFormElement()
      .element(by.css('.text-help'))
      .getText();
  },

  businessKeyInput: function(inputValue) {
    var inputField = this.genericFormElement().element(
      by.css('[cam-business-key]')
    );

    if (arguments.length !== 0) inputField.sendKeys(inputValue);

    return inputField;
  },

  addVariableButton: function() {
    return this.genericFormElement().element(
      by.css('[ng-click="addVariable()"]')
    );
  },

  addVariable: function(name, type, value) {
    var that = this;

    this.addVariableButton().click();
    this.variableList()
      .count()
      .then(function(idx) {
        idx = idx - 1;
        that.variableNameInput(idx, name);
        that.variableTypeInput(idx, type);
        if (type === 'Boolean') {
          if (value === 'true' || value === true)
            that.variableValueInput(idx).click();
        } else {
          that.variableValueInput(idx, value);
        }
      });
  },

  removeVariableButton: function(idx) {
    return this.genericFormElement().element(
      by.css('[ng-click="removeVariable(delta)"]')
    );
  },

  removeVariable: function(idx) {
    this.removeVariableButton().click();
  },

  variableList: function() {
    return this.genericFormElement().all(
      by.repeater('(delta, variable) in variables')
    );
  },

  variableInputElement: function(rowIdx, colIdx) {
    return this.variableList()
      .get(rowIdx)
      .element(by.css('.row .col-xs-4:nth-child(' + colIdx + ')'));
  },

  variableInputField: function(rowIdx, colIdx, inputValue) {
    var inputField = this.variableInputElement(rowIdx, colIdx).element(
      by.css('.form-control')
    );

    if (arguments.length === 3) inputField.sendKeys(inputValue);

    return inputField;
  },

  variableInputHelpText: function(rowIdx, colIdx) {
    return this.variableInputElement(rowIdx, colIdx)
      .element(by.css('.has-error .help-block'))
      .getText();
  },

  isVariableInputFieldValide: function(rowIdx, colIdx) {
    return this.variableInputElement(rowIdx, colIdx)
      .getAttribute('class')
      .then(function(classes) {
        return classes.indexOf('ng-valide') !== -1;
      });
  },

  variableNameInput: function(idx, inputValue) {
    return this.variableInputField(idx, 1, inputValue);
  },

  variableTypeInput: function(idx, inputValue) {
    return this.variableInputField(idx, 2, inputValue);
  },

  variableValueInput: function(idx, inputValue) {
    return this.variableInputField(idx, 3, inputValue);
  },

  variableNameHelpText: function(idx) {
    return this.variableInputHelpText(idx, 1);
  },

  variableValueHelpText: function(idx) {
    return this.variableInputHelpText(idx, 3);
  },

  isVariableNameInputValide: function(idx) {
    return this.isVariableInputFieldValide(idx, 1);
  },

  isVariableValueInputValide: function(idx) {
    return this.isVariableInputFieldValide(idx, 3);
  }
});
