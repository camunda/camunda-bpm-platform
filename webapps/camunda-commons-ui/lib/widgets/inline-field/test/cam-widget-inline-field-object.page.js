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

function Field(node) {
  this.node = node;
}

Field.prototype.isPresent = function() {
  return this.node.isPresent();
};
Field.prototype.click = function() {
  this.node.click();
  return this;
};
Field.prototype.clear = function() {
  this.node.element(by.tagName('input')).clear();
  return this;
};
Field.prototype.type = function() {
  this.node.element(by.tagName('input')).sendKeys.apply(this, arguments);
  return this;
};
Field.prototype.text = function() {
  return this.node.getText();
};
Field.prototype.inputText = function() {
  return this.node.element(by.tagName('input')).getAttribute('value');
};
Field.prototype.inputField = function() {
  return this.node.element(by.css('input[ng-model="editValue"]'));
};
Field.prototype.okButton = function() {
  return element(by.css('.glyphicon-ok'));
};
Field.prototype.cancelButton = function() {
  return element(by.css('.glyphicon-remove'));
};
Field.prototype.calendarButton = function() {
  return element(by.css('.glyphicon-calendar'));
};
Field.prototype.pencilButton = function() {
  return element(by.css('.glyphicon-pencil'));
};
Field.prototype.datepicker = function() {
  return element(by.css('.datepicker'));
};
Field.prototype.datepicker.day = function(day) {
  return this().element(by.cssContainingText('button > span:not(.text-muted)', day));
};
Field.prototype.datepicker.activeDay = function() {
  return this().element(by.css('button.active')).getText();
};
Field.prototype.timepicker = function() {
  return element(by.className('timepicker'));
};
Field.prototype.timepicker.hoursField = function() {
  return this().element(by.model('hours'));
};
Field.prototype.timepicker.hoursValue = function() {
  return this().element(by.model('hours')).getAttribute('value');
};
Field.prototype.timepicker.minutesField = function() {
  return this().element(by.model('minutes'));
};
Field.prototype.timepicker.minutesValue = function() {
  return this().element(by.model('minutes')).getAttribute('value');
};
Field.prototype.dropdown = function() {
  return this.node.element(by.className('dropdown-menu'));
};
Field.prototype.dropdownOption = function(option) {
  return this.dropdown().all(by.tagName('a')).get(option);
};
Field.prototype.dropdownOptionByText = function(text) {
  return this.dropdown().element(by.linkText(text));
};
Field.prototype.dropdownOptionCount = function() {
  return this.dropdown().all(by.tagName('li')).count();
};

module.exports = Field;
