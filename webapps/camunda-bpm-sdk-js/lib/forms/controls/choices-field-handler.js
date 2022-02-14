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
  $ = require('./../dom-lib');

/**
 * A field control handler for choices
 * @class
 * @memberof CamSDK.form
 * @augments {CamSDK.form.AbstractFormField}
 */
var ChoicesFieldHandler = AbstractFormField.extend(
  /** @lends CamSDK.form.ChoicesFieldHandler.prototype */
  {
    /**
     * Prepares an instance
     */
    initialize: function() {
      // read variable definitions from markup
      var variableName = (this.variableName = this.element.attr(
        constants.DIRECTIVE_CAM_VARIABLE_NAME
      ));
      var variableType = (this.variableType = this.element.attr(
        constants.DIRECTIVE_CAM_VARIABLE_TYPE
      ));
      var choicesVariableName = (this.choicesVariableName = this.element.attr(
        constants.DIRECTIVE_CAM_CHOICES
      ));

      // crate variable
      this.variableManager.createVariable({
        name: variableName,
        type: variableType,
        value: this.element.val() || null
      });

      // fetch choices variable
      if (choicesVariableName) {
        this.variableManager.fetchVariable(choicesVariableName);
      }

      // remember the original value found in the element for later checks
      this.originalValue = this.element.val() || null;

      this.previousValue = this.originalValue;

      // remember variable name
      this.variableName = variableName;
    },

    /**
     * Applies the stored value to a field element.
     *
     * @return {CamSDK.form.ChoicesFieldHandler} Chainable method.
     */
    applyValue: function() {
      var selectedIndex = this.element[0].selectedIndex;
      // if cam-choices variable is defined, apply options
      if (this.choicesVariableName) {
        var choicesVariableValue = this.variableManager.variableValue(
          this.choicesVariableName
        );
        if (choicesVariableValue) {
          // array
          if (choicesVariableValue instanceof Array) {
            for (var i = 0; i < choicesVariableValue.length; i++) {
              var val = choicesVariableValue[i];
              if (!this.element.find('option[text="' + val + '"]').length) {
                this.element.append(
                  $('<option>', {
                    value: val,
                    text: val
                  })
                );
              }
            }
            // object aka map
          } else {
            for (var p in choicesVariableValue) {
              if (!this.element.find('option[value="' + p + '"]').length) {
                this.element.append(
                  $('<option>', {
                    value: p,
                    text: choicesVariableValue[p]
                  })
                );
              }
            }
          }
        }
      }

      // make sure selected index is retained
      this.element[0].selectedIndex = selectedIndex;

      // select option referenced in cam-variable-name (if any)
      this.previousValue = this.element.val() || '';
      var variableValue = this.variableManager.variableValue(this.variableName);

      // check if variable is defined before writing values to the html
      // variableValue can be the Number 0 or negative, so `|| ''` does not work here
      variableValue = (variableValue === null) ? '' : variableValue;

      if (variableValue !== this.previousValue) {
        // write value to html control
        this.element.val(variableValue);
        this.element.trigger('camFormVariableApplied', variableValue);

        // Update the ui after the current digest cycle
        var that = this;
        window.setTimeout(function() {
          that.element.change();
        }, 0);
      }

      return this;
    },

    /**
     * Retrieves the value from a field element and stores it
     *
     * @return {*} when multiple choices are possible an array of values, otherwise a single value
     */
    getValue: function() {
      // read value from html control
      var value;
      var multiple = this.element.prop('multiple');

      if (multiple) {
        value = [];
        this.element.find('option:selected').each(function() {
          value.push($(this).val());
        });
      } else {
        value = this.element.find('option:selected').attr('value');
      }

      // write value to variable
      this.variableManager.variableValue(this.variableName, value);

      return value;
    }
  },
  /** @lends CamSDK.form.ChoicesFieldHandler */
  {
    selector: 'select[' + constants.DIRECTIVE_CAM_VARIABLE_NAME + ']'
  }
);

module.exports = ChoicesFieldHandler;
