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
  tabRepeater: 'tabProvider in processDefinitionTabs',
  tabIndex: 2,
  tabLabel: 'Job Definitions',
  tableRepeater: 'jobDefinition in jobDefinitions',

  state: function(idx) {
    return this.tableItem(idx, '.state:not(.ng-hide)');
  },

  activity: function(idx) {
    return this.tableItem(idx, '.activity');
  },

  configuration: function(idx) {
    return this.tableItem(idx, '.configuration');
  },

  suspendJobDefinitionButton: function(idx) {
    return this.tableItem(
      idx,
      '[ng-click="openSuspensionStateDialog(jobDefinition)"]:not(.ng-hide)'
    );
  },

  activateJobDefinitionButton: function(idx) {
    return this.suspendJobDefinitionButton(idx);
  },

  suspendJobDefinition: function(idx) {
    var modal = this.modal;

    this.suspendJobDefinitionButton(idx)
      .click()
      .then(function() {
        browser.sleep(500);
        modal
          .suspendButton()
          .click()
          .then(function() {
            browser.sleep(500);
            modal.okButton().click();
          });
      });
  },

  activateJobDefinition: function(idx) {
    this.suspendJobDefinition(idx);
  }
});
