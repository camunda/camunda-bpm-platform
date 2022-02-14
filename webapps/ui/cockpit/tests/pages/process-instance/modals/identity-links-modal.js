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

var Base = require('./../../base');

module.exports = Base.extend({
  dialog: function() {
    return element(by.css('.identity-link-modal .modal-content'));
  },

  header: function() {
    return this.dialog().element(by.css('.modal-header'));
  },

  title: function() {
    return this.header()
      .element(by.css('.modal-title'))
      .getText();
  },

  body: function() {
    return this.dialog().element(by.css('.modal-body'));
  },

  elements: function() {
    return this.body().all(
      by.repeater('(delta, identityLink) in identityLinks')
    );
  },

  elementName: function(idx) {
    return this.elements()
      .get(idx)
      .element(by.css('.id'))
      .getText();
  },

  nameInput: function() {
    return this.body().element(by.css('[ng-model="newItem"]'));
  },

  addNameButton: function() {
    return this.body().element(by.css('[ng-click="addItem()"]'));
  },

  clickAddNameButton: function() {
    return this.addNameButton().click();
  },

  deleteNameButton: function(name) {
    var self = this;
    return this.getNameIndex(name).then(function(idx) {
      return self
        .elements()
        .get(idx)
        .element(by.css('.action-button'));
    });
  },

  clickDeleteNameButton: function(name) {
    return this.deleteNameButton(name).then(function(elem) {
      return elem.click();
    });
  },

  getNameIndex: function(name) {
    return this.findElementIndexInRepeater(
      '(delta, identityLink) in identityLinks',
      by.css('.id'),
      name
    ).then(function(idx) {
      return idx;
    });
  },

  footer: function() {
    return this.dialog().element(by.css('.modal-footer'));
  },

  closeButton: function() {
    return this.footer().element(by.cssContainingText('.btn', 'Close'));
  },

  clickCloseButton: function() {
    return this.closeButton().click();
  }
});
