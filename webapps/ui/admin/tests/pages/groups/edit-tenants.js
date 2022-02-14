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

var Page = require('./edit');

var formElement = element(by.css('form[name="updateTenantMemberships"]'));

module.exports = Page.extend({
  url: '/camunda/app/admin/default/#/groups/:group?tab=tenants',

  subHeader: function() {
    return formElement.element(by.css('.h4')).getText();
  },

  tenantList: function() {
    return formElement.all(by.repeater('tenant in tenantList'));
  },

  tenantId: function(idx) {
    return this.tenantList()
      .get(idx)
      .element(by.binding('{{ tenant.id }}'))
      .getText();
  },

  openAddTenantModal: function() {
    var theElement = element(by.css('.modal-header'));
    this.addTenantButton().click();
    this.waitForElementToBeVisible(theElement, 5000);
  },

  addTenantButton: function() {
    return element(by.css('[ng-click="openCreateTenantMembershipDialog()"]'));
  },

  removeTenant: function(idx) {
    this.tenantList()
      .get(idx)
      .element(by.css('[ng-click="removeTenant(tenant.id)"]'))
      .click();
  }
});
