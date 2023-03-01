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

var sinon = require('sinon');

var defaultResourceMethods = [
  'create',
  'list',
  'count',
  'update',
  'delete',
  'get'
];

/**
 * Creates mocked camAPI instance, that has resource method that always returns fakeResource property of mocked camAPI.
 *
 * @param resourceMethods   methods that should be mocked on resource.
 * @returns {{resource: *, fakeResource: *}}
 */
function createCamApiMock(resourceMethods) {
  resourceMethods = resourceMethods || defaultResourceMethods;

  var fakeResource = resourceMethods.reduce(function(fakeResource, method) {
    fakeResource[method] = noop;

    sinon.stub(fakeResource, method).callsFake(function() {
      var args = Array.prototype.slice.call(arguments);
      var callback = args[args.length - 1];

      if (typeof callback === 'function') {
        callback(null, args.slice(0, -1));
      }
    });

    return fakeResource;
  }, {});

  var camAPI = {
    resource: sinon.stub(),
    fakeResource: fakeResource
  };

  camAPI.resource.returns(fakeResource);

  return camAPI;
}

createCamApiMock.defaultResourceMethods = defaultResourceMethods.slice(); // Copy for safety

module.exports = createCamApiMock;

function noop() {}
