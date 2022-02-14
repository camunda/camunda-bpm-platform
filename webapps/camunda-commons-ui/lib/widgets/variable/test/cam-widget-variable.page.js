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

function Variable(node) {
  this.node = node;
}

Variable.prototype.editingGroup = function() {
  return this.node.element(by.css('.editing.input-group'));
};
Variable.prototype.editingGroupClass = function() {
  return this.editingGroup().getAttribute('class');
};

Variable.prototype.setNullBtn = function() {
  return this.editingGroup().element(by.css('.btn.set-null'));
};
Variable.prototype.setNonNullBtn = function() {
  return this.editingGroup().element(by.css('.btn.null-value'));
};

Variable.prototype.type = function() {
  return this.node.element(by.css('.type'));
};
Variable.prototype.typeSelectElement = function(w00t) {
  return this.type().element(by.css('select'));
};
Variable.prototype.typeSelected = function() {
  return this.typeSelectElement().getAttribute('value');
};
Variable.prototype.typeSelect = function(w00t) {
  this.typeSelectElement().click();
  return this.type().element(by.cssContainingText('option', w00t));
};
Variable.prototype.typeCss = function() {
  return this.typeSelectElement().getAttribute('class');
};


Variable.prototype.name = function() {
  return this.node.element(by.css('.name'));
};
Variable.prototype.nameValue = function() {
  return this.name().getAttribute('value');
};
Variable.prototype.nameText = function() {
  return this.name().getText();
};
Variable.prototype.nameCss = function() {
  return this.name().getAttribute('class');
};


Variable.prototype.value = function() {
  return this.node.element(by.css('.value'));
};
Variable.prototype.valueModalLink = function() {
  return this.value().element(by.css('a'));
};
Variable.prototype.valueValue = function() {
  return this.value().getAttribute('value');
};
Variable.prototype.valueType = function() {
  return this.value().getAttribute('type');
};
Variable.prototype.valueText = function() {
  return this.value().getText();
};
Variable.prototype.valueCss = function() {
  return this.value().getAttribute('class');
};



function Modal(node) {
  this.node = node;
}

Modal.prototype.header = function() {
  return this.node.element(by.css('.modal-header'));
};
Modal.prototype.body = function() {
  return this.node.element(by.css('.modal-body'));
};
Modal.prototype.textareaSerialized = function() {
  return this.body().element(by.css('textarea[ng-model="variable.value"]'));
};
Modal.prototype.objectTypeInput = function() {
  return this.body().element(by.css('[ng-model="variable.valueInfo.objectTypeName"]'));
};
Modal.prototype.serializationTypeInput = function() {
  return this.body().element(by.css('[ng-model="variable.valueInfo.serializationDataFormat"]'));
};
Modal.prototype.footer = function() {
  return this.node.element(by.css('.modal-footer'));
};
Modal.prototype.button = function(text) {
  return this.footer().element(by.cssContainingText('button', text));
};



function Page() { }

Page.prototype.variable = function(identifier, index) {
  return new Variable(element.all(by.css(identifier + ' [cam-widget-variable]')).get(index));
};

Page.prototype.modal = function() {
  return new Modal(element(by.css('body > .modal')));
};

Page.prototype.applyButton = function() {
  return element(by.css('form > button'));
};


module.exports = new Page();
