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

var Page = require('../../../common/tests/pages/page');

module.exports = Page.extend({
  suspendedBadge: function() {
    return element(by.css('.ctn-header .badge'));
  },

  navbar: function() {
    return element(by.css('[cam-widget-header]'));
  },

  navbarItems: function() {
    return this.navbar().all(by.css('[ng-transclude] > ul > li'));
  },

  navbarItem: function(idx) {
    return this.navbarItems().get(idx);
  },

  navbarItemClick: function() {
    return this.navbarItem()
      .element(by.css('a'))
      .click();
  },

  navbarDropDown: function() {
    return this.navbar().all(by.css('[ng-transclude] > ul > li.dropdown'));
  },

  navbarDropDownItems: function() {
    return this.navbarDropDown().all(by.css('.dropdown-menu > li'));
  },

  navbarDropDownItem: function(idx) {
    return this.navbarDropDownItems().get(idx);
  },

  navbarDropDownItemClick: function(idx) {
    this.navbarDropDown().click();
    return this.navbarDropDownItem(idx).click();
  },

  goToSection: function(name) {
    return this.navbar()
      .element(by.cssContainingText('[ng-transclude] > ul > li a', name))
      .click();
  }
});
