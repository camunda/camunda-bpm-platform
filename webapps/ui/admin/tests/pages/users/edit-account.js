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

var Page = require('./edit-base');

var changePasswordFormElement = element(
  by.css('form[name="updateCredentialsForm"]')
);
var deleteUserFormElement = element(
  by.css('[ng-if="availableOperations.delete"]')
);

module.exports = Page.extend({
  url: '/camunda/app/admin/default/#/users/:user?tab=account',

  subHeaderChangePassword: function() {
    return changePasswordFormElement.element(by.css('.h4')).getText();
  },

  subHeaderDeleteUser: function() {
    return deleteUserFormElement.element(by.css('.h4')).getText();
  },

  myPasswordInput: function(inputValue) {
    var inputField = element(by.model('credentials.authenticatedUserPassword'));

    if (arguments.length !== 0) inputField.sendKeys(inputValue);

    return inputField;
  },

  newPasswordInput: function(inputValue) {
    var inputField = element(by.model('credentials.password'));

    if (arguments.length !== 0) inputField.sendKeys(inputValue);

    return inputField;
  },

  newPasswordRepeatInput: function(inputValue) {
    var inputField = element(by.model('credentials.password2'));

    if (arguments.length !== 0) inputField.sendKeys(inputValue);

    return inputField;
  },

  changePasswordButton: function() {
    return changePasswordFormElement.element(
      by.css('[ng-click="updateCredentials()"]')
    );
  },

  changePassword: function(myPassword, newPassword, newPasswordRepeat) {
    this.myPasswordInput(myPassword);
    this.newPasswordInput(newPassword);
    this.newPasswordRepeatInput(newPasswordRepeat);
    this.changePasswordButton().click();
  },

  deleteUserButton: function() {
    return deleteUserFormElement.element(by.css('[ng-click="deleteUser()"]'));
  },

  deleteUserAlert: function() {
    return browser.switchTo().alert();
  },

  deleteUser: function() {
    this.deleteUserButton().click();
    element(by.css('.modal-footer [ng-click="$close()"]')).click();
    browser.sleep(100);
  }
});
