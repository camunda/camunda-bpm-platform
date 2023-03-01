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

module.exports = {
  setup1: combine(
    operation('filter', 'create', [
      {
        name: 'All',
        query: {},
        properties: {
          priority: 100,
          description: 'Show all Tasks'
        },
        resourceType: 'Task'
      },
      {
        name: 'My Tasks',
        query: {
          assigneeExpression: '${ currentUser() }'
        },
        properties: {
          priority: 10
        },
        resourceType: 'Task'
      },
      {
        name: 'Test Filter',
        query: {},
        properties: {
          priority: 110
        },
        resourceType: 'Task'
      }
    ]),

    operation('user', 'create', [
      {
        id: 'test',
        firstName: 'Montgomery',
        lastName: 'QA',
        password: 'test'
      }
    ]),

    operation('authorization', 'create', [
      {
        type: 1,
        permissions: ['ALL'],
        userId: 'test',
        groupId: null,
        resourceType: 0,
        resourceId: 'tasklist'
      },
      {
        type: 1,
        permissions: ['ALL'],
        userId: 'test',
        groupId: null,
        resourceType: 5,
        resourceId: '*'
      },
      {
        type: 1,
        permissions: ['READ'],
        userId: 'test',
        groupId: null,
        resourceType: 7,
        resourceId: '*'
      }
    ]),

    operation('task', 'create', [
      {
        id: '1',
        name: 'Task 1'
      },
      {
        id: '2',
        name: 'Task 2'
      },
      {
        id: '3',
        name: 'Task 3'
      }
    ]),

    operation('task', 'assignee', [
      {
        taskId: '1',
        userId: 'test'
      },
      {
        taskId: '2',
        userId: 'test'
      }
    ])
  )
};
