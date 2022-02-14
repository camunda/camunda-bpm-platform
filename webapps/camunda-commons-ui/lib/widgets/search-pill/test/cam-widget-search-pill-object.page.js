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

var InlineField = require('../../inline-field/test/cam-widget-inline-field-object.page.js');

var Pill = function(node) {
  this.node = node;
};

Pill.prototype.typeElement = function() {
  return this.node.element(by.css('.type-field'));
};

Pill.prototype.nameElement = function() {
  return this.node.element(by.css('.name-field'));
};

Pill.prototype.operatorElement = function() {
  return this.node.element(by.css('.operator-field'));
};

Pill.prototype.valueElement = function() {
  return this.node.element(by.css('.value-field'));
};

Pill.prototype.typeField = function() {
  return new InlineField(this.typeElement());
};
Pill.prototype.nameField = function() {
  return new InlineField(this.nameElement());
};
Pill.prototype.operatorField = function() {
  return new InlineField(this.operatorElement());
};
Pill.prototype.valueField = function() {
  return new InlineField(this.valueElement());
};
Pill.prototype.isValid = function() {
  return this.node.element(by.className('search-label')).getAttribute('class').then(function(classes) {
    return classes.split(' ').indexOf('invalid') === -1;
  });
};

Pill.prototype.removeButton = function() {
  return this.node.element(by.css('.remove-search'));
};

module.exports = Pill;
