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

/**
  Defines a filter to convert given milliseconds into
  human readable time lapse.

  @name duration
  @memberof cam.cockpit.filter
  @type angular.filter

  @author Roman Smirnov <roman.smirnov@camunda.com>
 */
'use strict';

var Filter = function() {
  return function(duration) {
    if (!duration) {
      return;
    }

    var x = duration;
    var milliseconds = Math.floor(x % 1000);
    x /= 1000;
    var seconds = Math.floor(x % 60);
    x /= 60;
    var minutes = Math.floor(x % 60);
    x /= 60;
    var hours = Math.floor(x % 24);
    x /= 24;
    var days = Math.floor(x);

    var result = [];

    addValue(days, 'day', result);
    addValue(hours, 'hour', result);
    addValue(minutes, 'minute', result);
    addValue(seconds, 'second', result);

    if (result.length === 0) {
      addValue(milliseconds, 'millisecond', result);
    }

    return result.join(', ');
  };

  function addValue(value, unit, array) {
    if (value) {
      var result = [];
      result.push(value);
      result.push(value === 1 ? unit : unit + 's');
      array.push(result.join(' '));
    }
  }
};
module.exports = Filter;
