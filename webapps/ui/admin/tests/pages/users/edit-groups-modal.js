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

var Page = require('./edit-groups');

module.exports = Page.extend({
  pageHeader: function() {
    return element(by.css('.modal-header')).getText();
  },

  groupList: function() {
    return element.all(by.repeater('group in availableGroups'));
  },

  selectGroup: function(idx) {
    this.groupList()
      .get(idx)
      .element(by.model('group.checked'))
      .click();
  },

  groupId: function(idx) {
    return this.groupList()
      .get(idx)
      .element(by.css('.group-id a'));
  },

  groupName: function(idx) {
    return this.groupList()
      .get(idx)
      .element(by.css('.group-name'));
  },

  addSelectedGroupButton: function() {
    return element(by.css('[ng-click="createGroupMemberships()"]'));
  },

  cancelButton: function() {
    return element(by.css('[ng-click="close()"]'));
  },

  okButton: function() {
    return element(by.css('[ng-click="close(status)"]'));
  },

  addGroup: function(idx) {
    var that = this;
    var theElement = this.groupList().get(idx);

    this.waitForElementToBeVisible(theElement, 5000);
    this.selectGroup(idx);
    this.addSelectedGroupButton()
      .click()
      .then(function() {
        that.okButton().click();
      });
  }
});
