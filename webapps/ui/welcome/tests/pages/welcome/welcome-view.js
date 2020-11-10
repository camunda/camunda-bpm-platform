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

var Base = require('./../base');

module.exports = Base.extend({
  url: '/camunda/app/welcome/default/#/welcome',

  webappLinks: function() {
    return element(by.css('.webapps'));
  },

  adminWebappLink: function() {
    return this.webappLinks().element(by.css('.admin-app'));
  },

  cockpitWebappLink: function() {
    return this.webappLinks().element(by.css('.cockpit-app'));
  },

  tasklistWebappLink: function() {
    return this.webappLinks().element(by.css('.tasklist-app'));
  },

  userProfile: function() {
    return element(by.css('#user-profile'));
  },

  userProfileFullName: function() {
    return this.userProfile().element(by.css('.user-profile-name'));
  },

  userProfileEmail: function() {
    return this.userProfile().element(by.css('.user-profile-email'));
  },

  userProfileGroups: function() {
    return this.userProfile().element(by.css('.user-profile-groups'));
  },

  userProfileLink: function() {
    return this.userProfile().element(
      by.cssContainingText('.action-links li a', 'Edit profile')
    );
  },

  changePasswordLink: function() {
    return this.userProfile().element(
      by.cssContainingText('.action-links li a', 'Change password')
    );
  },

  userProfileForm: function() {
    return this.userProfile().element(by.css('form[name=userProfile]'));
  },

  userProfileFirstNameField: function() {
    return this.userProfileForm().element(by.css('[name=firstName]'));
  },

  userProfileLastNameField: function() {
    return this.userProfileForm().element(by.css('[name=lastName]'));
  },

  userProfileEmailField: function() {
    return this.userProfileForm().element(by.css('[name=email]'));
  },

  userProfileFormSubmit: function() {
    return this.userProfileForm()
      .element(by.css('[type=submit]'))
      .click();
  },

  changePasswordForm: function() {
    return this.userProfile().element(by.css('form[name=changePassword]'));
  },

  changePasswordCurrentField: function() {
    return this.changePasswordForm().element(by.css('[name=current]'));
  },

  changePasswordNewField: function() {
    return this.changePasswordForm().element(by.css('[name=new]'));
  },

  changePasswordConfirmationField: function() {
    return this.changePasswordForm().element(by.css('[name=confirmation]'));
  },

  changePasswordFormSubmit: function() {
    return this.changePasswordForm()
      .element(by.css('[type=submit]'))
      .click();
  }
});
