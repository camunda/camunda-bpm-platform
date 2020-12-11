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

var Base = require('./base');

module.exports = Base.extend({
  crumb: function(index) {
    return element(
      by.css('.cam-breadcrumb [data-index="' + index + '"] a.text')
    );
  },

  selectCrumb: function(index) {
    this.breadcrumb(index).click();
  },

  activeCrumb: function() {
    return element(by.css('.cam-breadcrumb li.active > .text'));
  },

  activeCrumbViewSwitcher: function() {
    return element(by.css('.cam-breadcrumb li.active .switcher'));
  },

  activeCrumbViewSwitcherCurrent: function() {
    return element(by.css('.cam-breadcrumb li.active .switcher .current'));
  },

  activeCrumbViewSwitcherLink: function() {
    return element(by.css('.cam-breadcrumb li.active .switcher a'));
  },

  activeCrumbDropdown: function() {
    return element(by.css('.cam-breadcrumb li.active > .dropdown'));
  },

  activeCrumbDropdownLabel: function() {
    return element(by.css('.cam-breadcrumb li.active .dropdown-toggle'));
  },

  activeCrumbDropdownOpen: function() {
    return this.activeCrumbDropdownLabel().click();
  },

  activeCrumbDropdownSelect: function(what) {
    var self = this;
    return self.activeCrumbDropdownOpen().then(function() {
      self
        .activeCrumbDropdown()
        .element(by.cssContainingText('.dropdown-menu > li > a', what))
        .click();
    });
  }
});
