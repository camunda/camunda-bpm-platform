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
  url: '/camunda/app/admin/default/#/users',

  newUserButton: function() {
    return element(by.css('[ng-show="availableOperations.create"]'));
  },

  userList: function() {
    return element.all(by.repeater('user in userList'));
  },

  userFirstNameAndLastName: function(idx) {
    return element(
      by
        .repeater('user in userList')
        .row(idx)
        .column('{{user.firstName}} {{user.lastName}}')
    ).getText();
  },

  selectUser: function(idx) {
    this.selectUserByEditLink(idx);
  },

  selectUserByEditLink: function(idx) {
    this.userList()
      .get(idx)
      .element(by.linkText('Edit'))
      .click();
  },

  selectUserByNameLink: function(idx) {
    this.userList()
      .get(idx)
      .element(by.binding('{{user.firstName}} {{user.lastName}}'))
      .click();
  }
});
