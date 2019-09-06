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

var Loader = function(node) {
  this.node = node;
};
Loader.prototype.isPresent = function() {
  return this.node.isPresent();
};
Loader.prototype.stateText = function() {
  return this.node.element(by.css('.state-display')).getText();
};
Loader.prototype.defaultPanel = function() {
  return this.node.element(by.css('.panel.panel-default'));
};
Loader.prototype.loadingNotice = function() {
  return this.node.element(by.css('.loader-state.loading'));
};
Loader.prototype.errorNotice = function() {
  return this.node.element(by.css('.alert.alert-danger'));
};
Loader.prototype.reloadButton = function() {
  return this.node.element(by.css('button.reload'));
};
Loader.prototype.reloadEmptyButton = function() {
  return this.node.element(by.css('button.reload-empty'));
};
Loader.prototype.failButton = function() {
  return this.node.element(by.css('button.fail-load'));
};

function Page() { }

Page.prototype.loader = function(node) {
  return new Loader(element(by.css(node)));
};

module.exports = new Page();
