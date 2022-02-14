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

var Base = require('./deployed-decisions-plugin');

module.exports = Base.extend({
  listObject: function() {
    return this.pluginObject().element(by.css('.decision-definitions-list'));
  },

  decisionsList: function() {
    return this.listObject().all(by.repeater('decision in decisions'));
  },

  selectDecision: function(idx) {
    return this.decisionsList()
      .get(idx)
      .element(by.binding('{{ decision.name || decision.key }}'))
      .click();
  },

  selectDecisionByTenantId: function(tenantId) {
    var that = this;

    this.findElementIndexInRepeater(
      'decision in decisions',
      by.css('.tenant-id'),
      tenantId
    ).then(function(idx) {
      that.selectDecision(idx);
    });
  },

  decisionName: function(item) {
    return this.decisionsList()
      .get(item)
      .element(by.binding('{{ decision.name || decision.key }}'))
      .getText();
  },

  tenantId: function(item) {
    return this.decisionsList()
      .get(item)
      .element(by.css('.tenant-id'))
      .getText();
  }
});
