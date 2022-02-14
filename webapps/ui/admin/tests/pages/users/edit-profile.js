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

var formElement = element(by.css('form[name="editProfileForm"]'));

module.exports = Page.extend({
  url: '/camunda/app/admin/default/#/users/:user?tab=profile',

  subHeader: function() {
    return formElement.element(by.css('.h4')).getText();
  },

  firstNameInput: function(inputValue) {
    var inputField = element(by.model('profile.firstName'));

    if (arguments.length !== 0) inputField.sendKeys(inputValue);

    return inputField;
  },

  lastNameInput: function(inputValue) {
    var inputField = element(by.model('profile.lastName'));

    if (arguments.length !== 0) inputField.sendKeys(inputValue);

    return inputField;
  },

  emailInput: function(inputValue) {
    var inputField = element(by.model('profile.email'));

    if (arguments.length !== 0) inputField.sendKeys(inputValue);

    return inputField;
  },

  updateProfileButton: function() {
    return formElement.element(by.css('[ng-click="updateProfile()"]'));
  },

  changeUserProfile: function(firstName, lastName) {
    this.firstNameInput().clear();
    this.lastNameInput().clear();
    this.firstNameInput(firstName);
    this.lastNameInput(lastName);
    this.updateProfileButton().click();
  }
});
