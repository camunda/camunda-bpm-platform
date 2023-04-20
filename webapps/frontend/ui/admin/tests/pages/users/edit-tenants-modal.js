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

var Page = require('./edit-tenants');

module.exports = Page.extend({
  pageHeader: function() {
    return element(by.css('.modal-header')).getText();
  },

  tenantList: function() {
    return element.all(by.repeater('tenant in availableTenants'));
  },

  selectTenant: function(idx) {
    this.tenantList()
      .get(idx)
      .element(by.model('tenant.checked'))
      .click();
  },

  tenantId: function(idx) {
    return this.tenantList()
      .get(idx)
      .element(by.css('.tenant-id a'));
  },

  tenantName: function(idx) {
    return this.tenantList()
      .get(idx)
      .element(by.css('.tenant-name'));
  },

  addSelectedTenantButton: function() {
    return element(by.css('[ng-click="createUserMemberships()"]'));
  },

  cancelButton: function() {
    return element(by.css('[ng-click="close()"]'));
  },

  okButton: function() {
    return element(by.css('[ng-click="close(status)"]'));
  },

  selectAllCheckbox: function() {
    return element(by.css('[ng-click="checkAllTenants()"'));
  },

  addTenants: function() {
    var that = this;
    var theElement = this.tenantList().get(0);

    this.waitForElementToBeVisible(theElement, 5000);
    this.selectAllCheckbox().click();
    this.addSelectedTenantButton()
      .click()
      .then(function() {
        that.okButton().click();
      });
  },

  addTenant: function(idx) {
    var that = this;
    var theElement = this.tenantList().get(idx);

    this.waitForElementToBeVisible(theElement, 5000);
    this.selectTenant(idx);
    this.addSelectedTenantButton()
      .click()
      .then(function() {
        that.okButton().click();
      });
  }
});
