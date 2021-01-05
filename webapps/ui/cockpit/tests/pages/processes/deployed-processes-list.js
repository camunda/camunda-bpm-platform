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

var Base = require('./deployed-processes-plugin');

module.exports = Base.extend({
  tabLabel: 'List',

  listObject: function() {
    return element(by.css('.process-definitions-list'));
  },

  processesList: function() {
    return this.listObject().all(by.css('tbody tr'));
  },

  selectProcess: function(item) {
    return this.processesList()
      .get(item)
      .element(by.css('.name a'))
      .click();
  },

  selectProcessByName: function(name) {
    return this.listObject()
      .element(by.cssContainingText('tbody tr .name a', name))
      .click();
  },

  processName: function(item) {
    return this.processesList()
      .get(item)
      .element(by.css('.name a'))
      .getText();
  },

  runningInstances: function(item) {
    return this.processesList()
      .get(item)
      .element(by.binding('{{ pd.instances }}'))
      .getText();
  },

  tenantId: function(item) {
    return this.processesList()
      .get(item)
      .element(by.css('.tenant-id'))
      .getText();
  },

  getReportColumn: function() {
    return this.pluginObject().element(by.css('th.report-link'));
  }
});
