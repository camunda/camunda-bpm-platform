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
  AbstractFormField = require('./abstract-form-field');

/**
 * A field control handler for file downloads
 * @class
 * @memberof CamSDK.form
 * @augments {CamSDK.form.AbstractFormField}
 */
var InputFieldHandler = AbstractFormField.extend(
  {
    /**
     * Prepares an instance
     */
    initialize: function() {
      this.variableName = this.element.attr(
        constants.DIRECTIVE_CAM_FILE_DOWNLOAD
      );

      // fetch the variable
      this.variableManager.fetchVariable(this.variableName);
    },

    applyValue: function() {
      var variable = this.variableManager.variable(this.variableName);

      // set the download url of the link
      this.element.attr('href', variable.contentUrl);

      // sets the text content of the link to the filename it the textcontent is empty
      if (this.element.text().trim().length === 0) {
        this.element.text(variable.valueInfo.filename);
      }

      return this;
    }
  },

  {
    selector: 'a[' + constants.DIRECTIVE_CAM_FILE_DOWNLOAD + ']'
  }
);

module.exports = InputFieldHandler;
