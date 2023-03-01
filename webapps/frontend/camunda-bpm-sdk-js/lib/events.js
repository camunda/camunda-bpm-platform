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

/**
 * Events handling utility which can be used on
 * any kind of object to provide `on`, `once`, `off`
 * and `trigger` functions.
 *
 * @exports CamSDK.Events
 * @mixin
 *
 * @example
 * var obj = {};
 * Events.attach(obj);
 *
 * obj.on('event:name', function() {});
 * obj.once('event:name', function() {});
 * obj.trigger('event:name', data, moreData, evenMoreData);
 */

var Events = {};

/**
 * Converts an object into array
 * @param  {*} obj
 * @return {Array}
 */
function toArray(obj) {
  var a,
    arr = [];
  for (a in obj) {
    arr.push(obj[a]);
  }
  return arr;
}

/**
 * Returns a function that will be executed
 * at most one time, no matter how often you call it.
 * @param  {Function} func
 * @return {Function}
 */
function once(func) {
  var ran = false,
    memo;
  return function() {
    if (ran) return memo;
    ran = true;
    memo = func.apply(this, arguments);
    func = null;
    return memo;
  };
}

/**
 * Ensure an object to have the needed _events property
 * @param  {*} obj
 * @param  {String} name
 */
function ensureEvents(obj, name) {
  obj._events = obj._events || {};
  obj._events[name] = obj._events[name] || [];
}

/**
 * Add the relevant Events methods to an object
 * @param  {*} obj
 */
Events.attach = function(obj) {
  obj.on = this.on;
  obj.once = this.once;
  obj.off = this.off;
  obj.trigger = this.trigger;
  obj._events = {};
};

/**
 * Bind a callback to `eventName`
 * @param  {String}   eventName
 * @param  {Function} callback
 */
Events.on = function(eventName, callback) {
  ensureEvents(this, eventName);

  this._events[eventName].push(callback);

  return this;
};

/**
 * Bind a callback who will only be called once to `eventName`
 * @param  {String}   eventName
 * @param  {Function} callback
 */
Events.once = function(eventName, callback) {
  var self = this;
  var cb = once(function() {
    self.off(eventName, once);
    callback.apply(this, arguments);
  });
  cb._callback = callback;
  return this.on(eventName, cb);
};

/**
 * Unbind one or all callbacks originally bound to `eventName`
 * @param  {String}   eventName
 * @param  {Function} [callback]
 */
Events.off = function(eventName, callback) {
  ensureEvents(this, eventName);

  if (!callback) {
    delete this._events[eventName];
    return this;
  }

  var e,
    arr = [];
  for (e in this._events[eventName]) {
    if (this._events[eventName][e] !== callback) {
      arr.push(this._events[eventName][e]);
    }
  }
  this._events[eventName] = arr;

  return this;
};

/**
 * Call the functions bound to `eventName`
 * @param  {String} eventName
 * @param {...*} [params]
 */
Events.trigger = function() {
  var args = toArray(arguments);
  var eventName = args.shift();
  ensureEvents(this, eventName);

  var e;
  for (e in this._events[eventName]) {
    this._events[eventName][e](this, args);
  }

  return this;
};

module.exports = Events;
