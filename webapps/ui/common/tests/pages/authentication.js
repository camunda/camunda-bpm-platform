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

var Page = require('./page');

module.exports = Page.extend({
  url: '/camunda/app/:webapp/default/#/login',

  formElement: function() {
    return element(by.css('form[name="signinForm"]'));
  },

  loginButton: function() {
    return this.formElement().element(by.css('[type="submit"]'));
  },

  usernameInput: function(inputValue) {
    var inputField = this.formElement().element(by.model('username'));

    if (arguments.length !== 0) inputField.sendKeys(inputValue);

    return inputField;
  },

  passwordInput: function(inputValue) {
    var inputField = this.formElement().element(by.model('password'));

    if (arguments.length !== 0) inputField.sendKeys(inputValue);

    return inputField;
  },

  userLogin: function(username, password) {
    this.usernameInput().clear();
    this.passwordInput().clear();
    this.usernameInput(username);
    this.passwordInput(password);
    this.loginButton().click();
  },

  ensureUserLogout: function() {
    var self = this;
    var el = element(by.css('.account.dropdown'));
    el.isPresent().then(function(yepNope) {
      if (yepNope) {
        self.userLogout();
      }
    });
  },

  userLogout: function() {
    element(by.css('.account.dropdown > .dropdown-toggle')).click();
    element(by.css('.account.dropdown > .dropdown-menu > .logout > a')).click();
  }
});
