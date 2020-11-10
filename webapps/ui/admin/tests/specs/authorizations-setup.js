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

var factory = require('../../../common/tests/setup-factory.js'),
  combine = factory.combine,
  operation = factory.operation;

var fragment1 = combine(
  operation('user', 'create', [
    {
      id: 'john',
      password: 'MobyDick',
      firstName: 'John',
      lastName: 'Bonham',
      email: 'john.bonham@led-zeppelin.com'
    },
    {
      id: 'ringo',
      password: 'cam123',
      firstName: 'Ringo',
      lastName: 'Starr',
      email: 'ringo.starr@the-beatles.com'
    }
  ])
);

var authBatch2 = [];
for (var i = 0; i < 10; i++) {
  authBatch2.push({
    type: 1,
    resourceType: 7,
    resourceId: '1',
    permissions: ['CREATE'],
    userId: 'a' + i
  });
}

var fragment2 = combine(operation('authorization', 'create', authBatch2));

var authBatch3 = [];
for (var i = 0; i < 45; i++) {
  authBatch3.push({
    type: 1,
    resourceType: 7,
    resourceId: '1',
    permissions: ['CREATE'],
    userId: 'xxxxxxxxxxx' + i
  });
}

var fragment3 = combine(operation('authorization', 'create', authBatch3));

module.exports = {
  setup1: fragment1,
  setup2: fragment2,
  setup3: fragment3
};
