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

var Page = require('./system-base');

module.exports = Page.extend({
  url: '/camunda/app/admin/default/#/system?section=system-settings-metrics',

  flowNodesResult: function() {
    return element(by.binding('metrics.flowNodes')).getText();
  },

  decisionElementsResult: function() {
    return element(by.binding('metrics.decisionElements')).getText();
  },

  startDateField: function(inputValue) {
    var inputField = element(by.model('startDate'));

    if (arguments.length !== 0) inputField.sendKeys(inputValue);

    return inputField;
  },

  endDateField: function(inputValue) {
    var inputField = element(by.model('endDate'));

    if (arguments.length !== 0) inputField.sendKeys(inputValue);

    return inputField;
  },

  refreshButton: function() {
    return element(by.css('[ng-click="load()"]'));
  }
});
