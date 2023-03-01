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

var listeners = [];

module.exports = {
  patchRequest: function(request) {

    // original method
    var end = request.Request.prototype.end;

    // override end function
    request.Request.prototype.end = function() {
      for(var i = 0; i < listeners.length; i++) {
        listeners[i](this, arguments);
      }
      end.apply(this, arguments);
    };

  },

  register: function(fn) {
    listeners.push(fn);
  },

  unregister: function(fn) {
    listeners.splice(listeners.indexOf(fn), 1);
  }
};
