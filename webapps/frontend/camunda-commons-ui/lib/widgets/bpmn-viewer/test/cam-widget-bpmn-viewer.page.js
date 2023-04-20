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
          element: false, expect: false, by: false, protractor: false, $: false */
'use strict';

var ViewerNode = function(node) {
  this.node = node;
};

ViewerNode.prototype.click = function() {
  return this.node.click();
};

ViewerNode.prototype.hover = function() {
  browser.actions().mouseMove(this.node).perform();
};

ViewerNode.prototype.isHighlighted = function() {
  return this.node.getAttribute('class').then(function(classes) {
    return classes.split(' ').indexOf('highlight') !== -1;
  });
};

var Viewer = function(node) {
  this.node = node;
};

Viewer.prototype.isPresent = function() {
  return this.node.isPresent();
};

Viewer.prototype.element = function(id) {
  return new ViewerNode(this.node.element(by.css('[data-element-id="'+id+'"]')));
};

Viewer.prototype.badgeFor = function(id) {
  return this.node.element(by.css('[data-container-id="' + id + '"] > .djs-overlay:first-child'));
};

Viewer.prototype.navigationButtons = function() {
  return this.node.element(by.css('.navigation.zoom'));
};

Viewer.prototype.zoomInButton = function() {
  return this.node.element(by.css('.btn.in'));
};

Viewer.prototype.zoomOutButton = function() {
  return this.node.element(by.css('.btn.out'));
};

Viewer.prototype.resetZoomButton = function() {
  return this.node.element(by.css('.navigation.reset > button'));
};

Viewer.prototype.zoomLevel = function() {
  return this.node.element(by.css('.viewport')).getAttribute('transform').then(function(transform) {
    return parseFloat(transform.substr(7));
  });
};

function Page() { }

Page.prototype.diagram = function(id) {
  return new Viewer(element(by.id(id)));
};

Page.prototype.hoveredElementsText = function() {
  return element(by.id('hoveredElementsList')).getText();
};

module.exports = new Page();
