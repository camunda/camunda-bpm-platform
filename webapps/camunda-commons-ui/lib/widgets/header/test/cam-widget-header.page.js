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

/* jshint node: true, unused: false */
/* global __dirname: false, describe: false, before: false, it: false, browser: false,
          element: false, expect: false, by: false, protractor: false */
'use strict';

function Header(node) {
  this.node = node;
}

Header.prototype.transcludedElement = function() {
  return this.node.element(by.css('[ng-transclude]'));
};
Header.prototype.transcludedText = function() {
  return this.transcludedElement().getText();
};
Header.prototype.account = function() {
  return this.node.element(by.css('.app-menu .account'));
};
Header.prototype.accountText = function() {
  return this.node.element(by.css('.app-menu .account > a')).getText();
};
Header.prototype.appSwitch = function() {
  return this.node.element(by.css('.app-menu .app-switch'));
};
Header.prototype.welcomeLink = function() {
  return this.appSwitch().element(by.css('.welcome'));
};
Header.prototype.adminLink = function() {
  return this.appSwitch().element(by.css('.admin'));
};
Header.prototype.cockpitLink = function() {
  return this.appSwitch().element(by.css('.cockpit'));
};
Header.prototype.tasklistLink = function() {
  return this.appSwitch().element(by.css('.tasklist'));
};
Header.prototype.hamburgerButton = function() {
  return this.node.element(by.css('.navbar-toggle'));
};
Header.prototype.smallScreenWarning = function() {
  return this.node.element(by.css('.small-screen-warning'));
};

function Page() { }

Page.prototype.header = function(identifier) {
  return new Header(element(by.css(identifier)));
};


module.exports = new Page();
