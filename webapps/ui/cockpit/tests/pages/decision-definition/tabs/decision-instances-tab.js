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

var Table = require('./../../table');

module.exports = Table.extend({
  tabRepeater: 'tabProvider in decisionDefinitionTabs',
  tabIndex: 0,
  tabLabel: 'Decision Instances',
  tableRepeater: 'decisionInstance in decisionInstances',

  selectInstanceId: function(idx) {
    this.waitForElementToBeVisible(element(by.repeater(this.tableRepeater)));
    return this.tableItem(idx, by.binding('decisionInstance.id')).click();
  },

  selectProcessDefinitionKey: function(idx) {
    this.waitForElementToBeVisible(element(by.repeater(this.tableRepeater)));
    return this.tableItem(
      idx,
      by.binding('decisionInstance.processDefinitionKey')
    ).click();
  },

  selectProcessInstanceId: function(idx) {
    this.waitForElementToBeVisible(element(by.repeater(this.tableRepeater)));
    return this.tableItem(
      idx,
      by.binding('decisionInstance.processInstanceId')
    ).click();
  },

  instanceId: function(idx) {
    return this.tableItem(idx, '[title]').getAttribute('title');
  }
});
