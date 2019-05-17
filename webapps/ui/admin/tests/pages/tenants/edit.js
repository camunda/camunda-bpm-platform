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

var Page = require('./../base');

module.exports = Page.extend({
  url: '/camunda/app/admin/default/#/tenants/:tenant?tab=tenant',

  updateTenantButton: function() {
    return element(by.css('[ng-click="updateTenant()"]'));
  },

  tenantNameInput: function(inputValue) {
    var inputField = element(by.model('tenant.name'));

    if (arguments.length !== 0) inputField.sendKeys(inputValue);

    return inputField;
  },

  deleteTenantButton: function() {
    return element(by.css('[ng-click="deleteTenant()"]'));
  },

  deleteTenantAlert: function() {
    return browser.switchTo().alert();
  },

  deleteTenant: function() {
    this.deleteTenantButton().click();
    element(by.css('.modal-footer [ng-click="$close()"]')).click();
  },

  selectUserNavbarItem: function(navbarItem) {
    var index = ['Information', 'Groups', 'Users'];

    var item;
    var itemIndex = index.indexOf(navbarItem) + 1;

    if (itemIndex) {
      item = element(by.css('aside ul li:nth-child(' + itemIndex + ')'));
    } else {
      item = element(by.css('aside ul li:nth-child(1)'));
    }

    item.click();
    return item;
  }
});
