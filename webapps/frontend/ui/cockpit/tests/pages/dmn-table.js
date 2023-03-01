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

var Base = require('./base');

var LabelRow = function(node) {
  this.node = node;
};

LabelRow.prototype.getInputText = function(idx) {
  return this.node
    .all(by.css('td.input'))
    .get(idx)
    .getText();
};

var RuleRow = function(node) {
  this.node = node;
};
RuleRow.prototype.getCellText = function(idx) {
  return this.node
    .all(by.css('td'))
    .get(idx)
    .getText();
};

module.exports = Base.extend({
  tableElement: function() {
    return element(by.css('[cam-widget-dmn-viewer]'));
  },

  row: function(idx) {
    return this.tableElement()
      .all(by.css('tbody > tr'))
      .get(idx);
  },

  labelRow: function() {
    return new LabelRow(this.tableElement().element(by.css('tr.labels')));
  },

  ruleRow: function(idx) {
    return new RuleRow(this.row(idx));
  }
});
