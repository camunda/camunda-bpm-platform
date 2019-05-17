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

module.exports = HoverArea;

function HoverArea() {
  this._hovered = null;
  this._listeners = [];
}

HoverArea.prototype.hoverTitle = function(title) {
  this._hovered = title;

  this._fireListeners();
};

HoverArea.prototype.cleanHover = function() {
  this._hovered = null;

  this._fireListeners();
};

HoverArea.prototype.addHoverListener = function(title, listener) {
  var entry = {
    title: title,
    listener: listener
  };

  this._listeners.push(entry);
  this._fireEntry(entry);

  return function() {
    this._listeners = this._listeners.filter(function(entry) {
      return entry.listener !== listener;
    });
  }.bind(this);
};

HoverArea.prototype._fireListeners = function() {
  this._listeners.forEach(this._fireEntry.bind(this));
};

HoverArea.prototype._fireEntry = function(entry) {
  entry.listener(entry.title === this._hovered);
};
