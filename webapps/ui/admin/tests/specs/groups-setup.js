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
  readResource = factory.readResource,
  combine = factory.combine,
  operation = factory.operation;

function createEntities(factory) {
  var batch = [];
  for (var i = 0; i < 45; i++) {
    batch.push(factory(i));
  }
  return batch;
}

var fragment2 = combine(
  operation(
    'group',
    'create',
    createEntities(function(idx) {
      return {
        id: 'group' + idx,
        name: 'group' + idx,
        type: 'GROUP' + idx
      };
    })
  )
);

var fragment3 = combine(
  operation('group', 'create', [
    {
      id: 'accounting',
      name: 'Accounting',
      type: 'WORKFLOW'
    }
  ]),

  operation(
    'user',
    'create',
    createEntities(function(idx) {
      return {
        id: 'user' + idx,
        password: 'cam123',
        firstName: 'abc',
        lastName: 'def'
      };
    })
  ),

  operation(
    'group',
    'createMember',
    createEntities(function(idx) {
      return {
        id: 'accounting',
        userId: 'user' + idx
      };
    })
  )
);

module.exports = {
  setup1: combine(
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
    ]),

    operation('group', 'create', [
      {
        id: 'accounting',
        name: 'Accounting',
        type: 'WORKFLOW'
      },
      {
        id: 'sales',
        name: 'Sales',
        type: 'WORKFLOW'
      },
      {
        id: 'marketing',
        name: 'Marketing',
        type: 'WORKFLOW'
      }
    ]),

    operation('tenant', 'create', [
      {
        id: 'tenantOne',
        name: 'Tenant One'
      },
      {
        id: 'tenantTwo',
        name: 'Tenant Two'
      }
    ]),

    operation('group', 'createMember', [
      {
        id: 'marketing',
        userId: 'john'
      },
      {
        id: 'accounting',
        userId: 'john'
      },
      {
        id: 'sales',
        userId: 'john'
      }
    ])
  ),

  setup2: fragment2,

  setup3: fragment3
};
