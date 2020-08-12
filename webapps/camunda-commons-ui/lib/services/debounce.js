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

module.exports = [
  '$timeout',
  function($timeout) {
    /**
     * Debounce a function call, making it callable an arbitrary number of times before it is actually executed once.
     *
     * @memberof cam.common.services
     * @name debounce
     * @type angular.factory
     *
     * @param fn {function} the function to debounce
     * @param wait {number} the timeout after which the function is actually called
     *
     * @return {function} the function that can be called an arbitrary number of times
     *                    that will only be called when the wait interval elapsed
     */
    return function debounce(fn, wait) {
      var timer;

      var debounced = function() {
        var context = this,
          args = arguments;

        debounced.$loading = true;

        if (timer) {
          $timeout.cancel(timer);
        }

        timer = $timeout(function() {
          timer = null;
          debounced.$loading = false;
          fn.apply(context, args);
        }, wait);
      };

      return debounced;
    };
  }
];
