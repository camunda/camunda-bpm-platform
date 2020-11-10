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

var constants = require('./../constants'),
  AbstractFormField = require('./abstract-form-field'),
  convertToType = require('../type-util').convertToType;

var isBooleanCheckbox = function(element) {
  return (
    element.attr('type') === 'checkbox' &&
    element.attr(constants.DIRECTIVE_CAM_VARIABLE_TYPE) === 'Boolean'
  );
};

/**
 * A field control handler for simple text / string values
 * @class
 * @memberof CamSDK.form
 * @augments {CamSDK.form.AbstractFormField}
 */
var InputFieldHandler = AbstractFormField.extend(
  /** @lends CamSDK.form.InputFieldHandler.prototype */
  {
    /**
     * Prepares an instance
     */
    initialize: function() {
      // read variable definitions from markup
      var variableName = this.element.attr(
        constants.DIRECTIVE_CAM_VARIABLE_NAME
      );
      var variableType = this.element.attr(
        constants.DIRECTIVE_CAM_VARIABLE_TYPE
      );

      // crate variable
      this.variableManager.createVariable({
        name: variableName,
        type: variableType
      });

      // remember the original value found in the element for later checks
      this.originalValue = this.element.val();

      this.previousValue = this.originalValue;

      // remember variable name
      this.variableName = variableName;

      this.getValue();
    },

    /**
     * Applies the stored value to a field element.
     *
     * @return {CamSDK.form.InputFieldHandler} Chainable method
     */
    applyValue: function() {
      this.previousValue = this.getValueFromHtmlControl() || '';

      var variableValue = this.variableManager.variableValue(this.variableName);

      if (
        variableValue &&
        this.variableManager.isDateVariable(this.variableName)
      ) {
        var dateValue = new Date(variableValue);
        variableValue = convertToType(dateValue, 'Date');
      }

      if (variableValue !== this.previousValue) {
        // write value to html control
        this.applyValueToHtmlControl(variableValue);
        this.element.trigger('camFormVariableApplied', variableValue);
      }

      return this;
    },

    /**
     * Retrieves the value from an <input>
     * element and stores it in the Variable Manager
     *
     * @return {*}
     */
    getValue: function() {
      var value = this.getValueFromHtmlControl();

      // write value to variable
      this.variableManager.variableValue(this.variableName, value);

      return value;
    },

    getValueFromHtmlControl: function() {
      if (isBooleanCheckbox(this.element)) {
        return this.element.prop('checked');
      } else {
        return this.element.val();
      }
    },

    applyValueToHtmlControl: function(variableValue) {
      if (isBooleanCheckbox(this.element)) {
        this.element.prop('checked', variableValue);
      } else if (this.element[0].type !== 'file') {
        this.element.val(variableValue);
      }
    }
  },
  /** @lends CamSDK.form.InputFieldHandler */
  {
    selector:
      'input[' +
      constants.DIRECTIVE_CAM_VARIABLE_NAME +
      ']' +
      ',textarea[' +
      constants.DIRECTIVE_CAM_VARIABLE_NAME +
      ']'
  }
);

module.exports = InputFieldHandler;
