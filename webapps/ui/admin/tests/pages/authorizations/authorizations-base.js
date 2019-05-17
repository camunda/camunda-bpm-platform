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
  selectAuthorizationNavbarItem: function(navbarItem) {
    return element(by.cssContainingText('aside li', navbarItem)).click();
  },

  boxHeader: function() {
    return element(by.css('.section-content > header h3')).getText();
  },

  newAuthorizationButton: function() {
    return element.all(by.css('[ng-click="addNewAuthorization()"]')).first();
  },

  createNewButton: function() {
    return element.all(by.css('[ng-click="addNewAuthorization()"]')).last();
  },

  authorizationList: function() {
    return element.all(by.repeater('authorization in authorizations'));
  },

  getAuthorization: function(idx) {
    return this.authorizationList().get(idx);
  },

  userGroupButton: function(idx) {
    return this.getAuthorization(idx).element(by.css('a.input-group-addon'));
  },

  userGroupInput: function(idx) {
    return this.getAuthorization(idx).element(by.css('input.input-auth-name'));
  },

  applyEditButton: function(idx) {
    return this.getAuthorization(idx).element(by.css('button.btn-primary'));
  },

  cancelEditButton: function(idx) {
    return this.getAuthorization(idx).element(by.css('.action a.btn-default'));
  },

  authorizationIdentityType: function(idx) {
    return this.getAuthorization(idx)
      .element(by.css('.user.group > span:not(.ng-hide)'))
      .getAttribute('tooltip');
  },

  authorizationIdentity: function(idx) {
    return this.getAuthorization(idx)
      .element(by.css('.user.group'))
      .getText();
  },

  resourceInput: function(idx) {
    return this.getAuthorization(idx).element(by.css('.resource-id input'));
  },

  authorizationResource: function(idx) {
    return this.getAuthorization(idx)
      .element(by.css('.resource-id'))
      .getText();
  },

  createNewElement: function() {
    return this.authorizationList().last();
  },

  editButton: function(idx) {
    return this.getAuthorization(idx).element(
      by.cssContainingText('a', 'Edit')
    );
  },

  deleteButton: function(idx) {
    return this.getAuthorization(idx).element(
      by.cssContainingText('a', 'Delete')
    );
  },

  authorizationType: function(authType) {
    return element
      .all(by.css('.authorization-type'))
      .last()
      .element(by.cssContainingText('option', authType.toUpperCase()));
  },

  identityButton: function() {
    return this.createNewElement().element(by.css('.input-group-addon'));
  },

  isIdentityButtonGroup: function() {
    return this.identityButton()
      .getAttribute('tooltip')
      .then(function(classes) {
        return classes.indexOf('Group') !== -1;
      });
  },

  identityIdInputFiled: function(inputValue) {
    var inputField = this.createNewElement().element(
      by.model('authorization.identityId')
    );

    if (arguments.length !== 0) inputField.sendKeys(inputValue);

    return inputField;
  },

  permissionsField: function() {
    return this.createNewElement().element(
      by.css('.input-group .form-control-static')
    );
  },

  permissionsButton: function() {
    return this.createNewElement().element(by.css('.input-group button'));
  },

  permissionsDropdownList: function() {
    return this.createNewElement().all(
      by.repeater('perm in availablePermissions')
    );
  },

  selectPermission: function(index, permissionsType) {
    this.permissionsButton().click();

    if (arguments.length === 1 && typeof index === 'string') {
      var that = this;
      permissionsType = index;

      this.findElementIndexInRepeater(
        'perm in availablePermissions',
        by.css('a'),
        permissionsType
      ).then(function(idx) {
        that
          .permissionsDropdownList()
          .get(idx)
          .click();
      });
    } else {
      this.permissionsDropdownList(index)
        .get(index)
        .click();
    }
  },

  selectPermissionFor: function(idx, permission) {
    this.getAuthorization(idx)
      .element(by.css('.permissions button'))
      .click();

    this.getAuthorization(idx)
      .element(by.cssContainingText('.permissions a', permission))
      .click();
  },

  authorizationPermissions: function(idx) {
    return this.getAuthorization(idx)
      .element(by.css('.permissions'))
      .getText();
  },

  resourceIdField: function(inputValue) {
    var inputField = this.createNewElement().element(
      by.model('authorization.resourceId')
    );

    if (arguments.length !== 0) inputField.sendKeys(inputValue);

    return inputField;
  },

  submitNewAuthorizationButton: function() {
    return this.createNewElement().element(
      by.css('[ng-click="confirmUpdateAuthorization(authorization)"]')
    );
  },

  abortNewAuthorizationButton: function() {
    return this.createNewElement().element(
      by.css('[ng-click="cancelUpdateAuthorization(authorization)"]')
    );
  },

  createNewAuthorization: function(
    authType,
    identityType,
    identityId,
    permissions,
    resourceId
  ) {
    var that = this;

    this.createNewButton().click();
    this.authorizationType(authType).click();
    this.isIdentityButtonGroup().then(function(state) {
      if (state && identityType.toUpperCase() === 'USER') {
        that.identityButton().click();
      } else if (!state && identityType.toUpperCase() === 'GROUP') {
        that.identityButton().click();
      }
    });
    this.identityIdInputFiled().clear();
    this.identityIdInputFiled(identityId);
    this.selectPermission(permissions);
    this.resourceIdField().clear();
    this.resourceIdField(resourceId);
    this.submitNewAuthorizationButton().click();
  },

  deleteAuthorization: function(idx) {
    this.deleteButton(idx).click();
    this.waitForElementToBeVisible(
      element(by.css('[ng-click="performDelete()"]'))
    );
    element(by.css('[ng-click="performDelete()"]')).click();
    this.waitForElementToBeVisible(
      element(by.css('[ng-click="close(status)"]'))
    );
    element(by.css('[ng-click="close(status)"]')).click();
  }
});
