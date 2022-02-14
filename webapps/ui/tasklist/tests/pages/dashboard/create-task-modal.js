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

var Page = require('./dashboard-view');

module.exports = Page.extend({
  createTaskDialog: function() {
    return element(by.css('.modal .modal-content'));
  },

  openCreateDialog: function() {
    this.selectNavbarItem('Task');
    var taskNameFieldElement = element(by.css('.modal-content'));
    return this.waitForElementToBeVisible(taskNameFieldElement, 5000);
  },

  closeButton: function() {
    return this.createTaskDialog().element(by.css('[ng-click="$dismiss()"]'));
  },

  closeCreateDialog: function() {
    var closeButtonElement = this.closeButton();
    closeButtonElement.click();
    this.waitForElementToBeNotPresent(closeButtonElement, 5000);
  },

  saveButton: function() {
    return this.createTaskDialog().element(by.css('[ng-click="save()"]'));
  },

  saveTask: function() {
    var saveButtonElement = this.saveButton();
    saveButtonElement.click();
    this.waitForElementToBeNotPresent(saveButtonElement, 5000);
  },

  taskNameField: function() {
    return this.createTaskDialog().element(by.css('input[name="taskName"]'));
  },

  taskAssigneeField: function() {
    return this.createTaskDialog().element(
      by.css('input[name="taskAssignee"]')
    );
  },

  taskTenantIdField: function() {
    return this.createTaskDialog().element(
      by.css('select[name="taskTenantId"]')
    );
  },

  taskNameInput: function(inputValue) {
    var inputField = this.taskNameField();

    if (arguments.length !== 0) inputField.sendKeys(inputValue);

    return inputField;
  },

  taskAssigneeInput: function(inputValue) {
    var inputField = this.taskAssigneeField();

    if (arguments.length !== 0) inputField.sendKeys(inputValue);

    return inputField;
  }
});
